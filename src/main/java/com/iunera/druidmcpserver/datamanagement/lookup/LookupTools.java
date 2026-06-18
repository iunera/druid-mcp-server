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

package com.iunera.druidmcpserver.datamanagement.lookup;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class LookupTools {

    private final LookupRepository lookupRepository;
    private final ObjectMapper objectMapper;

    public LookupTools(LookupRepository lookupRepository,
                       ObjectMapper objectMapper) {
        this.lookupRepository = lookupRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get lookups configuration or status
     */
    @McpTool(description = "Get configuration or status of lookups for all or a specific tier. Parameters: [tier] (String, optional), [lookupName] (String, optional) to fetch a specific lookup, and [includeStatus] (Boolean, optional) to fetch lookup propagation status instead of configuration.")
    public String getLookups(
            @McpToolParam(description = "Name of the lookup tier (optional)", required = false) String tier,
            @McpToolParam(description = "Name of the lookup (optional)", required = false) String lookupName,
            @McpToolParam(description = "Whether to fetch lookup propagation status instead of configuration (optional)", required = false) Boolean includeStatus
    ) {
        try {
            if (includeStatus != null && includeStatus) {
                if (tier != null && !tier.trim().isEmpty()) {
                    JsonNode result = lookupRepository.getLookupStatusForTier(tier);
                    return objectMapper.writeValueAsString(result);
                }
                JsonNode result = lookupRepository.getLookupStatus();
                return objectMapper.writeValueAsString(result);
            }

            if (tier != null && !tier.trim().isEmpty()) {
                if (lookupName != null && !lookupName.trim().isEmpty()) {
                    JsonNode result = lookupRepository.getLookup(tier, lookupName);
                    return objectMapper.writeValueAsString(result);
                }
                JsonNode result = lookupRepository.getLookupsForTier(tier);
                return objectMapper.writeValueAsString(result);
            }

            JsonNode result = lookupRepository.getAllLookups();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting lookups: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process lookups request: %s", e.getMessage());
        }
    }

    /**
     * Manage lookup configuration
     */
    @McpTool(description = "Create, update, or delete a lookup configuration. Parameters: [action] (Enum: UPSERT, DELETE, required), [tier] (String, required), [lookupName] (String, required), and [configJson] (String, optional) containing the lookup spec.")
    public String manageLookup(
            @McpToolParam(description = "Action to perform: UPSERT, DELETE (required)", required = true) String action,
            @McpToolParam(description = "Name of the lookup tier (required)", required = true) String tier,
            @McpToolParam(description = "Name of the lookup (required)", required = true) String lookupName,
            @McpToolParam(description = "Lookup specification JSON string (required for UPSERT)", required = false) String configJson
    ) {
        try {
            if (action == null) {
                return "Error: [action] parameter is required";
            }
            if (tier == null || tier.trim().isEmpty()) {
                return "Error: [tier] parameter is required";
            }
            if (lookupName == null || lookupName.trim().isEmpty()) {
                return "Error: [lookupName] parameter is required";
            }

            switch (action.toUpperCase()) {
                case "UPSERT":
                    if (configJson == null || configJson.trim().isEmpty()) {
                        return "Error: [configJson] is required for UPSERT action";
                    }
                    Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
                    JsonNode result = lookupRepository.createOrUpdateLookup(tier, lookupName, config);
                    return objectMapper.writeValueAsString(result);

                case "DELETE":
                    JsonNode deleteResult = lookupRepository.deleteLookup(tier, lookupName);
                    return objectMapper.writeValueAsString(deleteResult);

                default:
                    return String.format("Error: Unsupported action '%s'. Supported: UPSERT, DELETE", action);
            }
        } catch (RestClientException e) {
            return String.format("Error executing lookup action '%s' on '%s' in tier '%s': %s", action, lookupName, tier, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process lookup action '%s' request on '%s' in tier '%s': %s", action, lookupName, tier, e.getMessage());
        }
    }
}
