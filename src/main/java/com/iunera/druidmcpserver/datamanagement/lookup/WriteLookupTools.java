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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
@Component
public class WriteLookupTools {

    private final LookupRepository lookupRepository;
    private final ObjectMapper objectMapper;

    public WriteLookupTools(LookupRepository lookupRepository,
                            ObjectMapper objectMapper) {
        this.lookupRepository = lookupRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create or update a lookup configuration
     */
    @McpTool(description = "Create or update a lookup configuration. Provide tier, lookup name, and configuration as JSON string")
    public String createOrUpdateLookup(String tier, String lookupName, String configJson) {
        try {
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            JsonNode result = lookupRepository.createOrUpdateLookup(tier, lookupName, config);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error creating/updating lookup '%s' in tier '%s': %s", lookupName, tier, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to create/update lookup '%s' in tier '%s': %s", lookupName, tier, e.getMessage());
        }
    }

    /**
     * Delete a lookup configuration
     */
    @McpTool(description = "Delete a lookup configuration by tier and name")
    public String deleteLookup(String tier, String lookupName) {
        try {
            JsonNode result = lookupRepository.deleteLookup(tier, lookupName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error deleting lookup '%s' in tier '%s': %s", lookupName, tier, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to delete lookup '%s' in tier '%s': %s", lookupName, tier, e.getMessage());
        }
    }

}