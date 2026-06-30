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
     * Retrieve retention rules or change history.
     */
    @McpTool(
            description = "Retrieve retention rules or audit history for a specific datasource or all datasources. Parameters: [datasource] (String, optional) to filter by a specific datasource, and [includeHistory] (Boolean, optional) to fetch change history instead of current rules.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, idempotentHint = true, destructiveHint = false)
    )
    public String getRetentionRules(
            @McpToolParam(description = "Name of the datasource (optional/required for history)", required = false) String datasource,
            @McpToolParam(description = "Whether to fetch change history instead of current rules (optional)", required = false) Boolean includeHistory
    ) {
        try {
            if (includeHistory != null && includeHistory) {
                if (datasource == null || datasource.trim().isEmpty()) {
                    return "Error: [datasource] is required when includeHistory is true";
                }
                JsonNode historyResult = retentionRulesRepository.getRetentionRuleHistory(datasource);
                return objectMapper.writeValueAsString(historyResult);
            }

            if (datasource != null && !datasource.trim().isEmpty()) {
                JsonNode result = retentionRulesRepository.getRetentionRulesForDatasource(datasource);
                return objectMapper.writeValueAsString(result);
            }

            JsonNode allResult = retentionRulesRepository.getAllRetentionRules();
            return objectMapper.writeValueAsString(allResult);
        } catch (RestClientException e) {
            return String.format("Error fetching retention rules (datasource: '%s', history: %s): %s", datasource, includeHistory, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process get retention rules request (datasource: '%s', history: %s): %s", datasource, includeHistory, e.getMessage());
        }
    }

    /**
     * Update/manage retention rules.
     */
    @McpTool(
            description = "Update retention rules configuration for a specific datasource. Parameters: [datasource] (String, required), and [rulesJson] (String, required) containing the JSON array of new retention rules.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, idempotentHint = false, destructiveHint = true)
    )
    public String manageRetentionRules(
            @McpToolParam(description = "Name of the datasource (required)", required = true) String datasource,
            @McpToolParam(description = "Retention rules JSON array (required)", required = true) String rulesJson
    ) {
        try {
            if (datasource == null || datasource.trim().isEmpty()) {
                return "Error: [datasource] is required";
            }
            if (rulesJson == null || rulesJson.trim().isEmpty()) {
                return "Error: [rulesJson] is required";
            }
            List<Map<String, Object>> rules = objectMapper.readValue(rulesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class,
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)));
            JsonNode updateResult = retentionRulesRepository.setRetentionRulesForDatasource(datasource, rules);
            return objectMapper.writeValueAsString(updateResult);
        } catch (RestClientException e) {
            return String.format("Error updating retention rules for datasource '%s': %s", datasource, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process update retention rules request for datasource '%s': %s", datasource, e.getMessage());
        }
    }
}
