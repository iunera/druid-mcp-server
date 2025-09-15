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
import org.springaicommunity.mcp.annotation.McpResource;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LookupResources {

    private final LookupRepository lookupRepository;
    private final ObjectMapper objectMapper;

    public LookupResources(LookupRepository lookupRepository,
                           ObjectMapper objectMapper) {
        this.lookupRepository = lookupRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get basic information for a specific lookup
     */
    @McpResource(uri = "lookup://{lookupid}", name = "Lookup", description = "Provides basic information for a specific Druid lookup")
    public ReadResourceResult getLookup(ReadResourceRequest request, String lookupid) {
        try {
            JsonNode allLookups = lookupRepository.getAllLookups();

            // Parse lookupid as "tier/lookupname"
            String[] parts = lookupid.split("/", 2);
            if (parts.length != 2) {
                String errorMessage = String.format("Invalid lookup ID format '%s'. Expected format: 'tier/lookupname'", lookupid);
                return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
            }

            String tierName = parts[0];
            String lookupName = parts[1];

            // Find the specific lookup
            JsonNode targetLookup = null;
            if (allLookups.isObject() && allLookups.has(tierName)) {
                JsonNode tierLookups = allLookups.get(tierName);
                if (tierLookups.isObject() && tierLookups.has(lookupName)) {
                    targetLookup = tierLookups.get(lookupName);
                }
            }

            if (targetLookup == null) {
                String errorMessage = String.format("Lookup '%s' not found in tier '%s'", lookupName, tierName);
                return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
            }

            String lookupJson = objectMapper.writeValueAsString(objectMapper.convertValue(targetLookup, Map.class));
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "application/json", lookupJson)));

        } catch (RestClientException e) {
            String errorMessage = String.format("Error retrieving lookup '%s': %s", lookupid, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to process lookup '%s': %s", lookupid, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
        }
    }

    /**
     * Get detailed information for a specific lookup including configuration details
     */
    @McpResource(uri = "lookup-details://{lookupid}", name = "Lookup Details", description = "Provides detailed information for a specific Druid lookup including configuration details")
    public ReadResourceResult getLookupDetails(String lookupid) {
        try {
            JsonNode allLookups = lookupRepository.getAllLookups();

            // Parse lookupid as "tier/lookupname"
            String[] parts = lookupid.split("/", 2);
            if (parts.length != 2) {
                String errorMessage = String.format("Invalid lookup ID format '%s'. Expected format: 'tier/lookupname'", lookupid);
                return new ReadResourceResult(List.of(new TextResourceContents("lookup-details://" + lookupid, "text/plain", errorMessage)));
            }

            String tierName = parts[0];
            String lookupName = parts[1];

            // Find the specific lookup
            JsonNode targetLookup = null;
            if (allLookups.isObject() && allLookups.has(tierName)) {
                JsonNode tierLookups = allLookups.get(tierName);
                if (tierLookups.isObject() && tierLookups.has(lookupName)) {
                    targetLookup = tierLookups.get(lookupName);
                }
            }

            if (targetLookup == null) {
                String errorMessage = String.format("Lookup '%s' not found in tier '%s'", lookupName, tierName);
                return new ReadResourceResult(List.of(new TextResourceContents("lookup-details://" + lookupid, "text/plain", errorMessage)));
            }

            // Get detailed information for this lookup
            Map<String, Object> lookupInfo = buildLookupInfo(targetLookup, tierName, lookupName);
            String lookupJson = objectMapper.writeValueAsString(lookupInfo);

            return new ReadResourceResult(List.of(new TextResourceContents("lookup-details://" + lookupid, "application/json", lookupJson)));

        } catch (RestClientException e) {
            String errorMessage = String.format("Error retrieving lookup details for '%s': %s", lookupid, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents("lookup-details://" + lookupid, "text/plain", errorMessage)));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to process lookup details for '%s': %s", lookupid, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents("lookup-details://" + lookupid, "text/plain", errorMessage)));
        }
    }

    /**
     * Build lookup information from the lookup configuration data
     */
    private Map<String, Object> buildLookupInfo(JsonNode lookupConfig, String tierName, String lookupName) {
        Map<String, Object> lookupInfo = new HashMap<>();

        lookupInfo.put("tier", tierName);
        lookupInfo.put("lookup_name", lookupName);

        // Add lookup configuration fields
        if (lookupConfig.has("version")) {
            lookupInfo.put("version", lookupConfig.get("version").asText());
        }
        if (lookupConfig.has("lookupExtractorFactory")) {
            JsonNode extractorFactory = lookupConfig.get("lookupExtractorFactory");
            lookupInfo.put("extractor_factory", extractorFactory);

            // Extract common extractor factory fields
            if (extractorFactory.has("type")) {
                lookupInfo.put("extractor_type", extractorFactory.get("type").asText());
            }
            if (extractorFactory.has("uri")) {
                lookupInfo.put("extractor_uri", extractorFactory.get("uri").asText());
            }
            if (extractorFactory.has("namespace")) {
                lookupInfo.put("namespace", extractorFactory.get("namespace").asText());
            }
        }

        // Add the full configuration for reference
        lookupInfo.put("full_config", lookupConfig);

        return lookupInfo;
    }
}
