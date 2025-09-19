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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
@Component
public class WriteRetentionRulesTools {

    private final RetentionRulesRepository retentionRulesRepository;
    private final ObjectMapper objectMapper;

    public WriteRetentionRulesTools(RetentionRulesRepository retentionRulesRepository,
                                    ObjectMapper objectMapper) {
        this.retentionRulesRepository = retentionRulesRepository;
        this.objectMapper = objectMapper;
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

}