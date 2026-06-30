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
        if (!druidProperties.getMcp().getSqlSyntaxCorrection().isEnabled()) {
            return sqlQuery;
        }

        try {
            // 1. Strip trailing semicolon
            String corrected = stripTrailingSemicolon(sqlQuery);

            // 2. Fetch schema metadata (cached)
            Map<String, Set<String>> metadata = metadataLoader.fetchSchemaMetadata();
            if (metadata.isEmpty()) {
                return corrected; // Fallback if schema couldn't be loaded
            }

            // 3. Extract and replace literals, quoted identifiers, and comments with placeholders
            PlaceholderContext placeholderContext = extractPlaceholders(corrected);
            String processedSql = placeholderContext.getProcessedSql();

            // 4. Correct and quote table names
            Set<String> referencedTables = new HashSet<>();
            processedSql = quoteTableNames(processedSql, metadata.keySet(), referencedTables);

            // 5. Correct and quote column names for the referenced tables
            processedSql = quoteColumnNames(processedSql, metadata, referencedTables);

            // 6. Restore comments, literals, and quoted identifiers in reverse order
            return restorePlaceholders(processedSql, placeholderContext);
        } catch (Exception e) {
            log.error("Error occurred during SQL syntax correction, falling back to original query: {}", e.getMessage(), e);
            return sqlQuery;
        }
    }

    /**
     * Strip trailing semicolon from the SQL query.
     * Example: "SELECT * FROM potsdam-V8;" -> "SELECT * FROM potsdam-V8"
     */
    private String stripTrailingSemicolon(String sql) {
        return sql.replaceAll(";\\s*$", "");
    }

    /**
     * Extract comments, literals, and quoted identifiers and replace them with placeholders.
     * This protects them from being incorrectly modified by search and replace logic.
     *
     * Example input: "SELECT city, 'potsdam-v8' AS name FROM potsdam-V8 WHERE city = \"Potsdam\" -- comment"
     * Example output: "SELECT city, __LITERAL_0__ AS name FROM potsdam-V8 WHERE city = __QUOTED_IDENTIFIER_0__ __LINE_COMMENT_0__"
     */
    private PlaceholderContext extractPlaceholders(String sql) {
        Pattern tokenPattern = Pattern.compile(
            "(/\\*[\\s\\S]*?\\*/)|(--.*)|('(?:''|[^'])*')|(\"(?:\"\"|[^\"])*\")"
        );
        Matcher matcher = tokenPattern.matcher(sql);
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
        
        return new PlaceholderContext(sb.toString(), blockComments, lineComments, literals, quotedIdentifiers);
    }

    /**
     * Correct the casing of table names and wrap them in double quotes.
     * Example: "... FROM potsdam-V8 ..." (official name is "potsdam-v8") -> "... FROM "potsdam-v8" ..."
     */
    private String quoteTableNames(String sql, Set<String> allTables, Set<String> outReferencedTables) {
        List<String> sortedTables = new ArrayList<>(allTables);
        // Sort tables by descending length to prevent partial matches (e.g. potsdam-v8 matching potsdam first)
        sortedTables.sort((a, b) -> Integer.compare(b.length(), a.length()));

        String boundaryChars = "[\\s\\(\\),;=\\!<>\\+\\*/]";
        String processedSql = sql;

        for (String table : sortedTables) {
            String escapedTable = Pattern.quote(table);
            // Regex matches the table name bounded by spaces, operators, commas, parentheses, etc.
            String regex = "(?i)(?<=^|" + boundaryChars + ")" + escapedTable + "(?=$|" + boundaryChars + ")";
            Pattern pattern = Pattern.compile(regex);
            Matcher tableMatcher = pattern.matcher(processedSql);
            
            if (tableMatcher.find()) {
                outReferencedTables.add(table);
                // Re-run replacement to double-quote and apply official casing
                String replacement = "\"" + table.replace("\"", "\"\"") + "\"";
                processedSql = tableMatcher.replaceAll(replacement);
            }
        }
        return processedSql;
    }

    /**
     * Correct and double-quote column names that belong to the referenced tables.
     * Example: "SELECT city FROM \"potsdam-v8\"" (official column is "city") -> "SELECT "city" FROM \"potsdam-v8\""
     */
    private String quoteColumnNames(String sql, Map<String, Set<String>> metadata, Set<String> referencedTables) {
        if (referencedTables.isEmpty()) {
            return sql;
        }

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

        String processedSql = sql;

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
        return processedSql;
    }

    /**
     * Restore comments, literals, and quoted identifiers in reverse order of token extraction.
     * Example input: "SELECT "city", __LITERAL_0__ AS name FROM \"potsdam-v8\" WHERE "city" = __QUOTED_IDENTIFIER_0__ __LINE_COMMENT_0__"
     * Example output: "SELECT "city", 'potsdam-v8' AS name FROM "potsdam-v8" WHERE "city" = "Potsdam" -- comment"
     */
    private String restorePlaceholders(String sql, PlaceholderContext context) {
        String processedSql = sql;
        List<String> quotedIdentifiers = context.getQuotedIdentifiers();
        List<String> literals = context.getLiterals();
        List<String> lineComments = context.getLineComments();
        List<String> blockComments = context.getBlockComments();

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
    }

    /**
     * Periodically evict schema metadata cache using configuration setting for TTL.
     */
    @Scheduled(fixedRateString = "${druid.mcp.sql-syntax-correction.cache-ttl-ms:300000}")
    @CacheEvict(value = "druidMetadata", allEntries = true)
    public void evictMetadataCache() {
        log.info("Evicting Druid schema metadata cache");
    }

    /**
     * Inner class to keep track of protected SQL elements and the processed query string.
     */
    private static class PlaceholderContext {
        private final String processedSql;
        private final List<String> blockComments;
        private final List<String> lineComments;
        private final List<String> literals;
        private final List<String> quotedIdentifiers;

        public PlaceholderContext(String processedSql,
                                  List<String> blockComments,
                                  List<String> lineComments,
                                  List<String> literals,
                                  List<String> quotedIdentifiers) {
            this.processedSql = processedSql;
            this.blockComments = blockComments;
            this.lineComments = lineComments;
            this.literals = literals;
            this.quotedIdentifiers = quotedIdentifiers;
        }

        public String getProcessedSql() {
            return processedSql;
        }

        public List<String> getBlockComments() {
            return blockComments;
        }

        public List<String> getLineComments() {
            return lineComments;
        }

        public List<String> getLiterals() {
            return literals;
        }

        public List<String> getQuotedIdentifiers() {
            return quotedIdentifiers;
        }
    }
}
