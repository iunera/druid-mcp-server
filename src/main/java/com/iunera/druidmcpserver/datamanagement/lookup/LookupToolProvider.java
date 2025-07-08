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
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class LookupToolProvider {

    private final LookupRepository lookupRepository;
    private final ObjectMapper objectMapper;

    public LookupToolProvider(LookupRepository lookupRepository,
                              ObjectMapper objectMapper) {
        this.lookupRepository = lookupRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * List all available Druid lookups from the coordinator
     */
    @Tool(description = "List all available Druid lookups from the coordinator")
    public String listLookups() {
        try {
            JsonNode result = lookupRepository.getAllLookups();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error listing lookups: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process lookups response: %s", e.getMessage());
        }
    }

    /**
     * Get lookup configurations for a specific tier
     */
    @Tool(description = "Get lookup configurations for a specific tier")
    public String getLookupsForTier(String tier) {
        try {
            JsonNode result = lookupRepository.getLookupsForTier(tier);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting lookups for tier '%s': %s", tier, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process lookups response for tier '%s': %s", tier, e.getMessage());
        }
    }

    /**
     * Get specific lookup configuration
     */
    @Tool(description = "Get configuration for a specific lookup by tier and name")
    public String getLookup(String tier, String lookupName) {
        try {
            JsonNode result = lookupRepository.getLookup(tier, lookupName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting lookup '%s' in tier '%s': %s", lookupName, tier, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process lookup response for '%s' in tier '%s': %s", lookupName, tier, e.getMessage());
        }
    }

    /**
     * Create or update a lookup configuration
     */
    @Tool(description = "Create or update a lookup configuration. Provide tier, lookup name, and configuration as JSON string")
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
    @Tool(description = "Delete a lookup configuration by tier and name")
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

    /**
     * Get lookup status for all tiers
     */
    @Tool(description = "Get lookup status for all tiers")
    public String getLookupStatus() {
        try {
            JsonNode result = lookupRepository.getLookupStatus();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting lookup status: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process lookup status response: %s", e.getMessage());
        }
    }

    /**
     * Get lookup status for a specific tier
     */
    @Tool(description = "Get lookup status for a specific tier")
    public String getLookupStatusForTier(String tier) {
        try {
            JsonNode result = lookupRepository.getLookupStatusForTier(tier);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting lookup status for tier '%s': %s", tier, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process lookup status response for tier '%s': %s", tier, e.getMessage());
        }
    }
}