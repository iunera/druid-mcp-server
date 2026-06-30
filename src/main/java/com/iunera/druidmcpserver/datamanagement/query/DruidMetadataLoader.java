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

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;
import tools.jackson.databind.JsonNode;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DruidMetadataLoader {

    private static final Logger log = LoggerFactory.getLogger(DruidMetadataLoader.class);
    private final RestClient druidRouterRestClient;

    public DruidMetadataLoader(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        try {
            fetchSchemaMetadata();
        } catch (Exception e) {
            log.debug("Cache warmup failed silently: {}", e.getMessage());
        }
    }

    /**
     * Fetch active Druid tables and columns from INFORMATION_SCHEMA.COLUMNS.
     * Annotated with @Cacheable to cache the result.
     */
    @Cacheable(value = "druidMetadata")
    public Map<String, Set<String>> fetchSchemaMetadata() {
        log.info("Fetching Druid schema metadata from INFORMATION_SCHEMA.COLUMNS");
        
        String sql = "SELECT TABLE_NAME, COLUMN_NAME FROM \"INFORMATION_SCHEMA\".\"COLUMNS\" WHERE \"TABLE_SCHEMA\" = 'druid'";
        Map<String, Object> query = new HashMap<>();
        query.put("query", sql);
        query.put("resultFormat", "object");

        try {
            JsonNode result = druidRouterRestClient
                    .post()
                    .uri("/druid/v2/sql")
                    .header("Content-Type", "application/json")
                    .body(query)
                    .retrieve()
                    .body(JsonNode.class);

            Map<String, Set<String>> metadata = new HashMap<>();
            if (result != null && result.isArray()) {
                for (JsonNode node : result) {
                    String tableName = node.has("TABLE_NAME") ? node.get("TABLE_NAME").asText() : null;
                    String columnName = node.has("COLUMN_NAME") ? node.get("COLUMN_NAME").asText() : null;
                    if (tableName != null && columnName != null) {
                        metadata.computeIfAbsent(tableName, k -> new HashSet<>()).add(columnName);
                    }
                }
            }
            log.info("Successfully fetched schema metadata for {} tables", metadata.size());
            return metadata;
        } catch (Exception e) {
            log.warn("Failed to fetch Druid schema metadata for syntax correction: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
