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

package com.iunera.druidmcpserver.datamanagement.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Repository
public class QueryRepository {

    public static final String SQL_ENDPOINT = "/druid/v2/sql";
    public static final String SQL_TASK_ENDPOINT = "/druid/v2/sql/task";

    private final RestClient druidRouterRestClient;
    private final ObjectMapper objectMapper;

    public QueryRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient,
                           ObjectMapper objectMapper) {
        this.druidRouterRestClient = druidRouterRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Execute a basic SQL query against Druid
     */
    public JsonNode executeSqlQuery(String sqlQuery) throws RestClientException {
        Map<String, Object> query = new HashMap<>();
        query.put("query", sqlQuery);
        query.put("resultFormat", "object");

        return druidRouterRestClient
                .post()
                .uri(SQL_ENDPOINT)
                .header("Content-Type", "application/json")
                .body(query)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Execute a multi-stage SQL query as a task
     */
    public JsonNode executeMultiStageSqlQuery(String sqlQuery) throws RestClientException {
        Map<String, Object> query = new HashMap<>();
        query.put("query", sqlQuery);
        query.put("resultFormat", "object");

        return druidRouterRestClient
                .post()
                .uri(SQL_TASK_ENDPOINT)
                .header("Content-Type", "application/json")
                .body(query)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Execute a multi-stage SQL query with custom context parameters
     */
    public JsonNode executeMultiStageSqlQueryWithContext(String sqlQuery, Map<String, Object> context) throws RestClientException {
        Map<String, Object> query = new HashMap<>();
        query.put("query", sqlQuery);
        query.put("resultFormat", "object");

        if (context != null && !context.isEmpty()) {
            query.put("context", context);
        }

        return druidRouterRestClient
                .post()
                .uri(SQL_TASK_ENDPOINT)
                .header("Content-Type", "application/json")
                .body(query)
                .retrieve()
                .body(JsonNode.class);
    }
}