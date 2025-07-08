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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Repository
public class LookupRepository {

    private final RestClient druidRouterRestClient;

    public LookupRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    /**
     * Get all lookup configurations
     */
    public JsonNode getAllLookups() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/lookups/config/all")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get lookup configuration for a specific tier
     */
    public JsonNode getLookupsForTier(String tier) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/lookups/config/{tier}", tier)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get specific lookup configuration
     */
    public JsonNode getLookup(String tier, String lookupName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/lookups/config/{tier}/{lookupName}", tier, lookupName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Create or update a lookup configuration
     */
    public JsonNode createOrUpdateLookup(String tier, String lookupName, Map<String, Object> lookupConfig) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/coordinator/v1/lookups/config/{tier}/{lookupName}", tier, lookupName)
                .header("Content-Type", "application/json")
                .body(lookupConfig)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Delete a lookup configuration
     */
    public JsonNode deleteLookup(String tier, String lookupName) throws RestClientException {
        return druidRouterRestClient
                .delete()
                .uri("/druid/coordinator/v1/lookups/config/{tier}/{lookupName}", tier, lookupName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get lookup status
     */
    public JsonNode getLookupStatus() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/lookups/status")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get lookup status for a specific tier
     */
    public JsonNode getLookupStatusForTier(String tier) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/lookups/status/{tier}", tier)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}
