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

package com.iunera.druidmcpserver.datamanagement.compaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class CompactionConfigToolProvider {

    private final CompactionConfigRepository compactionConfigRepository;
    private final ObjectMapper objectMapper;

    public CompactionConfigToolProvider(CompactionConfigRepository compactionConfigRepository,
                                        ObjectMapper objectMapper) {
        this.compactionConfigRepository = compactionConfigRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * View compaction configuration for all datasources
     */
    @McpTool(description = "View compaction configuration for all Druid datasources. Returns an array of compaction configurations.")
    public String viewAllCompactionConfigs() {
        try {
            JsonNode result = compactionConfigRepository.getAllCompactionConfigs();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving compaction configurations: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction configurations response: %s", e.getMessage());
        }
    }

    /**
     * View compaction configuration for a specific datasource
     */
    @McpTool(description = "View compaction configuration for a specific Druid datasource. Provide the datasource name as parameter.")
    public String viewCompactionConfigForDatasource(String datasourceName) {
        try {
            JsonNode result = compactionConfigRepository.getCompactionConfigForDatasource(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving compaction configuration for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction configuration response for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * Edit compaction configuration for a specific datasource
     */
    @McpTool(description = "Edit compaction configuration for a specific Druid datasource. Provide the datasource name and configuration as JSON string. Configuration should include dataSource, taskPriority, inputSegmentSizeBytes, maxRowsPerSegment, skipOffsetFromLatest, tuningConfig, taskContext, granularitySpec, dimensionsSpec, metricsSpec, and transformSpec.")
    public String editCompactionConfigForDatasource(String datasourceName, String configJson) {
        try {
            // Parse the configuration JSON string into a Map
            Map<String, Object> config = objectMapper.readValue(configJson,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

            // Ensure the datasource name is set in the config
            config.put("dataSource", datasourceName);

            JsonNode result = compactionConfigRepository.setCompactionConfigForDatasource(datasourceName, config);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error setting compaction configuration for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction configuration update for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * Delete compaction configuration for a specific datasource
     */
    @McpTool(description = "Delete compaction configuration for a specific Druid datasource. Provide the datasource name as parameter.")
    public String deleteCompactionConfigForDatasource(String datasourceName) {
        try {
            JsonNode result = compactionConfigRepository.deleteCompactionConfigForDatasource(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error deleting compaction configuration for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction configuration deletion for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * View compaction configuration history for a specific datasource
     */
    @McpTool(description = "View compaction configuration change history for a specific Druid datasource. Provide the datasource name as parameter.")
    public String viewCompactionConfigHistory(String datasourceName) {
        try {
            JsonNode result = compactionConfigRepository.getCompactionConfigHistory(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving compaction configuration history for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction configuration history response for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * View compaction status for all datasources
     */
    @McpTool(description = "View compaction status for all Druid datasources. Shows the current state of compaction tasks and progress.")
    public String viewCompactionStatus() {
        try {
            JsonNode result = compactionConfigRepository.getCompactionStatus();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving compaction status: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction status response: %s", e.getMessage());
        }
    }

    /**
     * View compaction status for a specific datasource
     */
    @McpTool(description = "View compaction status for a specific Druid datasource. Shows the current state of compaction tasks and progress for the datasource. Provide the datasource name as parameter.")
    public String viewCompactionStatusForDatasource(String datasourceName) {
        try {
            JsonNode result = compactionConfigRepository.getCompactionStatusForDatasource(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving compaction status for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction status response for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }
}