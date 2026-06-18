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

package com.iunera.druidmcpserver.datamanagement.retention;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class RetentionRulesTools {

    private final RetentionRulesRepository retentionRulesRepository;
    private final ObjectMapper objectMapper;

    public RetentionRulesTools(RetentionRulesRepository retentionRulesRepository,
                               ObjectMapper objectMapper) {
        this.retentionRulesRepository = retentionRulesRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get or manage retention rules
     */
    @McpTool(description = "View, fetch history, or update retention rules for a datasource. Parameters: [datasource] (String, optional), [action] (Enum: GET, GET_HISTORY, UPDATE, required), and [rulesJson] (String, optional) containing the retention rules JSON array.")
    public String getOrManageRetentionRules(
            @McpToolParam(description = "Action to perform: GET, GET_HISTORY, UPDATE (required)", required = true) String action,
            @McpToolParam(description = "Name of the datasource (optional for GET, required for GET_HISTORY and UPDATE)", required = false) String datasource,
            @McpToolParam(description = "Retention rules JSON array (required only for UPDATE action)", required = false) String rulesJson
    ) {
        try {
            if (action == null) {
                return "Error: [action] parameter is required";
            }

            switch (action.toUpperCase()) {
                case "GET":
                    if (datasource != null && !datasource.trim().isEmpty()) {
                        JsonNode result = retentionRulesRepository.getRetentionRulesForDatasource(datasource);
                        return objectMapper.writeValueAsString(result);
                    }
                    JsonNode allResult = retentionRulesRepository.getAllRetentionRules();
                    return objectMapper.writeValueAsString(allResult);

                case "GET_HISTORY":
                    if (datasource == null || datasource.trim().isEmpty()) {
                        return "Error: [datasource] is required for GET_HISTORY action";
                    }
                    JsonNode historyResult = retentionRulesRepository.getRetentionRuleHistory(datasource);
                    return objectMapper.writeValueAsString(historyResult);

                case "UPDATE":
                    if (datasource == null || datasource.trim().isEmpty()) {
                        return "Error: [datasource] is required for UPDATE action";
                    }
                    if (rulesJson == null || rulesJson.trim().isEmpty()) {
                        return "Error: [rulesJson] is required for UPDATE action";
                    }
                    List<Map<String, Object>> rules = objectMapper.readValue(rulesJson,
                            objectMapper.getTypeFactory().constructCollectionType(List.class,
                                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)));
                    JsonNode updateResult = retentionRulesRepository.setRetentionRulesForDatasource(datasource, rules);
                    return objectMapper.writeValueAsString(updateResult);

                default:
                    return String.format("Error: Unsupported action '%s'. Supported: GET, GET_HISTORY, UPDATE", action);
            }
        } catch (RestClientException e) {
            return String.format("Error executing retention rules action '%s' on datasource '%s': %s", action, datasource, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process retention rules action '%s' request on datasource '%s': %s", action, datasource, e.getMessage());
        }
    }
}
