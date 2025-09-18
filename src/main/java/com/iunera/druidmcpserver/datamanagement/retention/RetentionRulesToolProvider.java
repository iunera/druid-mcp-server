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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class RetentionRulesToolProvider {

    private final RetentionRulesRepository retentionRulesRepository;
    private final ObjectMapper objectMapper;

    public RetentionRulesToolProvider(RetentionRulesRepository retentionRulesRepository,
                                      ObjectMapper objectMapper) {
        this.retentionRulesRepository = retentionRulesRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * View retention rules for all datasources
     */
    @McpTool(description = "View retention rules for all Druid datasources. Returns a JSON object with datasource names as keys and their retention rules as values.")
    public String viewAllRetentionRules() {
        try {
            JsonNode result = retentionRulesRepository.getAllRetentionRules();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving retention rules: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process retention rules response: %s", e.getMessage());
        }
    }

    /**
     * View retention rules for a specific datasource
     */
    @McpTool(description = "View retention rules for a specific Druid datasource. Provide the datasource name as parameter.")
    public String viewRetentionRulesForDatasource(String datasourceName) {
        try {
            JsonNode result = retentionRulesRepository.getRetentionRulesForDatasource(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving retention rules for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process retention rules response for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * Edit retention rules for a specific datasource
     */
    @McpTool(description = "Edit retention rules for a specific Druid datasource. Provide the datasource name and rules as JSON string. Rules should be an array of rule objects with type, period, and other properties.")
    public String editRetentionRulesForDatasource(String datasourceName, String rulesJson) {
        try {
            // Parse the rules JSON string into a List of Maps
            List<Map<String, Object>> rules = objectMapper.readValue(rulesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class,
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)));

            JsonNode result = retentionRulesRepository.setRetentionRulesForDatasource(datasourceName, rules);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error setting retention rules for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process retention rules update for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * View retention rule history for a specific datasource
     */
    @McpTool(description = "View retention rule change history for a specific Druid datasource. Provide the datasource name as parameter.")
    public String viewRetentionRuleHistory(String datasourceName) {
        try {
            JsonNode result = retentionRulesRepository.getRetentionRuleHistory(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving retention rule history for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process retention rule history response for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }
}