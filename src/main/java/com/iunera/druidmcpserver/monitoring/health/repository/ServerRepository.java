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
 * Repository for server-related operations and status
 */
@Repository
public class ServerRepository {

    private final RestClient druidRouterRestClient;

    public ServerRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    /**
     * Get all servers status from router
     */
    public JsonNode getAllServersStatus() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/servers")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get servers status with full details
     */
    public JsonNode getAllServersStatusWithDetails() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/servers?full")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get specific server status
     */
    public JsonNode getServerStatus(String serverName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/servers/{serverName}", serverName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get broker status through router
     */
    public JsonNode getBrokerStatus() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/status")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}