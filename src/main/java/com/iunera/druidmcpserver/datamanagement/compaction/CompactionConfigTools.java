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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class CompactionConfigTools {

    private final CompactionConfigRepository compactionConfigRepository;
    private final ObjectMapper objectMapper;

    public CompactionConfigTools(CompactionConfigRepository compactionConfigRepository,
                                 ObjectMapper objectMapper) {
        this.compactionConfigRepository = compactionConfigRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get compaction configuration
     */
    @McpTool(description = "View compaction configuration or configuration change history for datasources. Parameters: [datasource] (String, optional), and [includeHistory] (Boolean, optional) to retrieve configuration history.")
    public String getCompactionConfig(
            @McpToolParam(description = "Name of the datasource (optional)", required = false) String datasource,
            @McpToolParam(description = "Whether to retrieve configuration change history (optional)", required = false) Boolean includeHistory
    ) {
        try {
            if (datasource != null && !datasource.trim().isEmpty()) {
                if (includeHistory != null && includeHistory) {
                    JsonNode result = compactionConfigRepository.getCompactionConfigHistory(datasource);
                    return objectMapper.writeValueAsString(result);
                }
                JsonNode result = compactionConfigRepository.getCompactionConfigForDatasource(datasource);
                return objectMapper.writeValueAsString(result);
            }
            JsonNode result = compactionConfigRepository.getAllCompactionConfigs();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting compaction config: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction config request: %s", e.getMessage());
        }
    }

    /**
     * Get compaction status
     */
    @McpTool(description = "Retrieve the current status of compaction runs and progress. Parameters: [datasource] (String, optional).")
    public String getCompactionStatus(
            @McpToolParam(description = "Name of the datasource to filter by (optional)", required = false) String datasource
    ) {
        try {
            if (datasource != null && !datasource.trim().isEmpty()) {
                JsonNode result = compactionConfigRepository.getCompactionStatusForDatasource(datasource);
                return objectMapper.writeValueAsString(result);
            }
            JsonNode result = compactionConfigRepository.getCompactionStatus();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting compaction status: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction status request: %s", e.getMessage());
        }
    }

    /**
     * Manage compaction configuration (UPSERT, DELETE)
     */
    @McpTool(description = "Add, update, or remove a compaction configuration. Parameters: [action] (Enum: UPSERT, DELETE, required), [datasource] (String, required), and [configJson] (String, optional) containing the compaction configuration spec.")
    public String manageCompaction(
            @McpToolParam(description = "Action to perform: UPSERT, DELETE (required)", required = true) String action,
            @McpToolParam(description = "Name of the datasource (required)", required = true) String datasource,
            @McpToolParam(description = "Compaction configuration JSON string (required for UPSERT)", required = false) String configJson
    ) {
        try {
            if (action == null) {
                return "Error: [action] parameter is required";
            }
            if (datasource == null || datasource.trim().isEmpty()) {
                return "Error: [datasource] parameter is required";
            }
            switch (action.toUpperCase()) {
                case "UPSERT":
                    if (configJson == null || configJson.trim().isEmpty()) {
                        return "Error: [configJson] is required for UPSERT action";
                    }
                    Map<String, Object> config = objectMapper.readValue(configJson,
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                    config.put("dataSource", datasource);
                    JsonNode upsertResult = compactionConfigRepository.setCompactionConfigForDatasource(datasource, config);
                    return objectMapper.writeValueAsString(upsertResult);

                case "DELETE":
                    JsonNode deleteResult = compactionConfigRepository.deleteCompactionConfigForDatasource(datasource);
                    return objectMapper.writeValueAsString(deleteResult);

                default:
                    return String.format("Error: Unsupported action '%s'. Supported: UPSERT, DELETE", action);
            }
        } catch (RestClientException e) {
            return String.format("Error executing compaction action '%s' on datasource '%s': %s", action, datasource, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process compaction action '%s' request on datasource '%s': %s", action, datasource, e.getMessage());
        }
    }
}
