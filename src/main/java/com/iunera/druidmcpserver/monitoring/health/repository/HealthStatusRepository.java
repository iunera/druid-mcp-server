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
 * Repository for core health status operations
 */
@Repository
public class HealthStatusRepository {

    private final RestClient druidRouterRestClient;

    public HealthStatusRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    /**
     * Get coordinator health status
     */
    public JsonNode getCoordinatorHealth() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/status/health")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get router health status
     */
    public JsonNode getRouterHealth() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/status/health")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get coordinator self-discovery status
     */
    public JsonNode getCoordinatorSelfDiscovered() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/status/selfDiscovered")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get router self-discovery status
     */
    public JsonNode getRouterSelfDiscovered() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/status/selfDiscovered")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get coordinator properties
     */
    public JsonNode getCoordinatorProperties() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/status/properties")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get router properties
     */
    public JsonNode getRouterProperties() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/status/properties")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}