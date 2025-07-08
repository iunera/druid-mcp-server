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

package com.iunera.druidmcpserver.ingestion.supervisors;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Repository
public class SupervisorsRepository {

    private final RestClient druidRouterRestClient;

    public SupervisorsRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    /**
     * List all supervisors
     */
    public JsonNode getAllSupervisors() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/supervisor")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get supervisor status by ID
     */
    public JsonNode getSupervisorStatus(String supervisorId) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/supervisor/{supervisorId}/status", supervisorId)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Suspend a supervisor
     */
    public JsonNode suspendSupervisor(String supervisorId) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/indexer/v1/supervisor/{supervisorId}/suspend", supervisorId)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Resume/Start a supervisor
     */
    public JsonNode resumeSupervisor(String supervisorId) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/indexer/v1/supervisor/{supervisorId}/resume", supervisorId)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Terminate a supervisor
     */
    public JsonNode terminateSupervisor(String supervisorId) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/indexer/v1/supervisor/{supervisorId}/terminate", supervisorId)
                .retrieve()
                .body(JsonNode.class);
    }
}
