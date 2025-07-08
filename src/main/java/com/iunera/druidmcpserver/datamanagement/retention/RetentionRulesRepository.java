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

package com.iunera.druidmcpserver.datamanagement.retention;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Repository
public class RetentionRulesRepository {

    private final RestClient druidRouterRestClient;

    public RetentionRulesRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;

    }

    /**
     * Get retention rules for all datasources
     */
    public JsonNode getAllRetentionRules() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/rules")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get retention rules for a specific datasource
     */
    public JsonNode getRetentionRulesForDatasource(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/rules/{datasourceName}", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Set retention rules for a specific datasource
     */
    public JsonNode setRetentionRulesForDatasource(String datasourceName, List<Map<String, Object>> rules) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/coordinator/v1/rules/{datasourceName}", datasourceName)
                .header("Content-Type", "application/json")
                .body(rules)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get retention rule history for a specific datasource
     */
    public JsonNode getRetentionRuleHistory(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/rules/{datasourceName}/history", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}
