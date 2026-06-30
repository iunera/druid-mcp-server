/*
 * Copyright (C) 2025 Christian Schmitt, Tim Frey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iunera.druidmcpserver.datamanagement.query;

import com.iunera.druidmcpserver.config.DruidProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SqlSyntaxCorrectionService {

    private static final Logger log = LoggerFactory.getLogger(SqlSyntaxCorrectionService.class);
    
    private final DruidMetadataLoader metadataLoader;
    private final DruidProperties druidProperties;
    
    // Set of SQL keywords to avoid auto-quoting as column names
    private static final Set<String> SQL_KEYWORDS = Set.of(
        "select", "from", "where", "group", "by", "order", "having", "limit",
        "and", "or", "not", "in", "is", "null", "like", "as", "join", "on",
        "left", "right", "inner", "outer", "cross", "union", "all", "sum",
        "count", "avg", "min", "max", "between", "exists", "case", "when",
        "then", "else", "end", "cast", "coalesce"
    );

    public SqlSyntaxCorrectionService(DruidMetadataLoader metadataLoader,
                                      DruidProperties druidProperties) {
        this.metadataLoader = metadataLoader;
        this.druidProperties = druidProperties;
    }

    /**
     * Correct query syntax by stripping semicolons, correcting datasource casings,
     * and quoting identifiers when not already quoted.
     */
    public String correctQuerySyntax(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            return sqlQuery;
        }

        // Return untouched if the feature is disabled
        if (!druidProperties.getSqlSyntaxCorrection().isEnabled()) {
            return sqlQuery;
        }

        try {
            // 1. Strip trailing semicolon
            // Example: "SELECT * FROM potsdam-V8;" -> "SELECT * FROM potsdam-V8"
            String corrected = sqlQuery.replaceAll(";\\s*$", "");

            // 2. Fetch schema metadata (cached)
            Map<String, Set<String>> metadata = metadataLoader.fetchSchemaMetadata();
            if (metadata.isEmpty()) {
                return corrected; // Fallback if schema couldn't be loaded
            }

            // 3. Extract and replace literals, quoted identifiers, and comments with placeholders
            // to protect them from being incorrectly modified.
            // Example input: "SELECT city, 'potsdam-v8' AS name FROM potsdam-V8 WHERE city = \"Potsdam\" -- comment"
            // Example output: "SELECT city, __LITERAL_0__ AS name FROM potsdam-V8 WHERE city = __QUOTED_IDENTIFIER_0__ __LINE_COMMENT_0__"
            Pattern tokenPattern = Pattern.compile(
                "(/\\*[\\s\\S]*?\\*/)|(--.*)|('(?:''|[^'])*')|(\"(?:\"\"|[^\"])*\")"
            );
            Matcher matcher = tokenPattern.matcher(corrected);
            StringBuilder sb = new StringBuilder();
            
            List<String> blockComments = new ArrayList<>();
            List<String> lineComments = new ArrayList<>();
            List<String> literals = new ArrayList<>();
            List<String> quotedIdentifiers = new ArrayList<>();
            
            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    blockComments.add(matcher.group(1));
                    matcher.appendReplacement(sb, "__BLOCK_COMMENT_" + (blockComments.size() - 1) + "__");
                } else if (matcher.group(2) != null) {
                    lineComments.add(matcher.group(2));
                    matcher.appendReplacement(sb, "__LINE_COMMENT_" + (lineComments.size() - 1) + "__");
                } else if (matcher.group(3) != null) {
                    literals.add(matcher.group(3));
                    matcher.appendReplacement(sb, "__LITERAL_" + (literals.size() - 1) + "__");
                } else if (matcher.group(4) != null) {
                    quotedIdentifiers.add(matcher.group(4));
                    matcher.appendReplacement(sb, "__QUOTED_IDENTIFIER_" + (quotedIdentifiers.size() - 1) + "__");
                }
            }
            matcher.appendTail(sb);
            String processedSql = sb.toString();

            // 4. Correct and quote table names
            // Example: "... FROM potsdam-V8 ..." (official name is "potsdam-v8")
            // After this step: "... FROM "potsdam-v8" ..."
            List<String> sortedTables = new ArrayList<>(metadata.keySet());
            // Sort tables by descending length to prevent partial matches (e.g. potsdam-v8 matching potsdam first)
            sortedTables.sort((a, b) -> Integer.compare(b.length(), a.length()));

            Set<String> referencedTables = new HashSet<>();
            String boundaryChars = "[\\s\\(\\),;=\\!<>\\+\\*/]";

            for (String table : sortedTables) {
                String escapedTable = Pattern.quote(table);
                // Regex matches the table name bounded by spaces, operators, commas, parentheses, etc.
                String regex = "(?i)(?<=^|" + boundaryChars + ")" + escapedTable + "(?=$|" + boundaryChars + ")";
                Pattern pattern = Pattern.compile(regex);
                Matcher tableMatcher = pattern.matcher(processedSql);
                
                if (tableMatcher.find()) {
                    referencedTables.add(table);
                    // Re-run replacement to double-quote and apply official casing
                    String replacement = "\"" + table.replace("\"", "\"\"") + "\"";
                    processedSql = tableMatcher.replaceAll(replacement);
                }
            }

            // 5. Correct and quote column names for the referenced tables
            // Example: "SELECT city FROM \"potsdam-v8\"" (official column is "city")
            // After this step: "SELECT "city" FROM \"potsdam-v8\""
            if (!referencedTables.isEmpty()) {
                Set<String> columnsToCorrect = new HashSet<>();
                for (String table : referencedTables) {
                    Set<String> cols = metadata.get(table);
                    if (cols != null) {
                        columnsToCorrect.addAll(cols);
                    }
                }

                List<String> sortedCols = new ArrayList<>(columnsToCorrect);
                // Sort columns by descending length to prevent partial matching
                sortedCols.sort((a, b) -> Integer.compare(b.length(), a.length()));

                for (String col : sortedCols) {
                    // Do not auto-quote SQL keywords (e.g., SELECT, LIMIT, avg)
                    if (SQL_KEYWORDS.contains(col.toLowerCase())) {
                        continue;
                    }

                    String escapedCol = Pattern.quote(col);
                    // Standard word boundaries are sufficient since columns are typically alphanumeric/underscores
                    String regex = "(?i)\\b" + escapedCol + "\\b";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher colMatcher = pattern.matcher(processedSql);

                    if (colMatcher.find()) {
                        String replacement = "\"" + col.replace("\"", "\"\"") + "\"";
                        processedSql = colMatcher.replaceAll(replacement);
                    }
                }
            }

            // 6. Restore comments, literals, and quoted identifiers in reverse order of token extraction
            // Example input: "SELECT "city", __LITERAL_0__ AS name FROM \"potsdam-v8\" WHERE "city" = __QUOTED_IDENTIFIER_0__ __LINE_COMMENT_0__"
            // Example output: "SELECT "city", 'potsdam-v8' AS name FROM "potsdam-v8" WHERE "city" = "Potsdam" -- comment"
            for (int i = 0; i < quotedIdentifiers.size(); i++) {
                processedSql = processedSql.replace("__QUOTED_IDENTIFIER_" + i + "__", quotedIdentifiers.get(i));
            }
            for (int i = 0; i < literals.size(); i++) {
                processedSql = processedSql.replace("__LITERAL_" + i + "__", literals.get(i));
            }
            for (int i = 0; i < lineComments.size(); i++) {
                processedSql = processedSql.replace("__LINE_COMMENT_" + i + "__", lineComments.get(i));
            }
            for (int i = 0; i < blockComments.size(); i++) {
                processedSql = processedSql.replace("__BLOCK_COMMENT_" + i + "__", blockComments.get(i));
            }

            return processedSql;
        } catch (Exception e) {
            log.error("Error occurred during SQL syntax correction, falling back to original query: {}", e.getMessage(), e);
            return sqlQuery;
        }
    }

    /**
     * Periodically evict schema metadata cache using configuration setting for TTL.
     */
    @Scheduled(fixedRateString = "${druid.sql-syntax-correction.cache-ttl-ms:300000}")
    @CacheEvict(value = "druidMetadata", allEntries = true)
    public void evictMetadataCache() {
        log.info("Evicting Druid schema metadata cache");
    }
}
