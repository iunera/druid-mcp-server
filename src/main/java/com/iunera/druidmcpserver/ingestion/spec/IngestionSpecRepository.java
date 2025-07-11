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

package com.iunera.druidmcpserver.ingestion.spec;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Repository
public class IngestionSpecRepository {

    private final RestClient druidRouterRestClient;

    public IngestionSpecRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    /**
     * Submit an ingestion spec to Druid router
     */
    public JsonNode submitIngestionSpec(Map<String, Object> ingestionSpec) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/indexer/v1/task")
                .header("Content-Type", "application/json")
                .body(ingestionSpec)
                .retrieve()
                .body(JsonNode.class);
    }
}
