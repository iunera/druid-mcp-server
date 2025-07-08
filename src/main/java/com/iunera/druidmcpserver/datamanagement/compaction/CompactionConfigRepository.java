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

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Repository
public class CompactionConfigRepository {

    private final RestClient druidRouterRestClient;

    public CompactionConfigRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    /**
     * Get compaction configuration for all datasources
     */
    public JsonNode getAllCompactionConfigs() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/config/compaction")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get compaction configuration for a specific datasource
     */
    public JsonNode getCompactionConfigForDatasource(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/config/compaction/{datasourceName}", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Set compaction configuration for a specific datasource
     */
    public JsonNode setCompactionConfigForDatasource(String datasourceName, Map<String, Object> config) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/coordinator/v1/config/compaction")
                .header("Content-Type", "application/json")
                .body(config)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Delete compaction configuration for a specific datasource
     */
    public JsonNode deleteCompactionConfigForDatasource(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .delete()
                .uri("/druid/coordinator/v1/config/compaction/{datasourceName}", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get compaction configuration history for a specific datasource
     */
    public JsonNode getCompactionConfigHistory(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/config/compaction/{datasourceName}/history", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get compaction status for all datasources
     */
    public JsonNode getCompactionStatus() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/compaction/status")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get compaction status for a specific datasource
     */
    public JsonNode getCompactionStatusForDatasource(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/compaction/status/{datasourceName}", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}
