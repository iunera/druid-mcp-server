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

package com.iunera.druidmcpserver.monitoring.health.repository;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Repository for cluster-level operations and metadata
 */
@Repository
public class ClusterRepository {

    private final RestClient druidRouterRestClient;

    public ClusterRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    /**
     * Get cluster configuration information
     */
    public JsonNode getClusterMetadata() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/config")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get cluster leader information
     */
    public JsonNode getLeaderInfo() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/leader")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Check if coordinator is leader
     */
    public JsonNode isCoordinatorLeader() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/isLeader")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}