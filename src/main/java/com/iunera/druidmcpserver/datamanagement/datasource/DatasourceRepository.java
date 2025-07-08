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

package com.iunera.druidmcpserver.datamanagement.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DatasourceRepository {

    private final RestClient druidRouterRestClient;
    private final ObjectMapper objectMapper;

    public DatasourceRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient,
                                ObjectMapper objectMapper) {
        this.druidRouterRestClient = druidRouterRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all datasources from Druid information schema
     */
    public JsonNode getAllDatasources() throws RestClientException {
        String sql = "SELECT * FROM \"INFORMATION_SCHEMA\".\"TABLES\" WHERE \"TABLE_SCHEMA\" = 'druid'";

        Map<String, Object> query = new HashMap<>();
        query.put("query", sql);
        query.put("resultFormat", "object");

        return druidRouterRestClient
                .post()
                .uri("/druid/v2/sql")
                .header("Content-Type", "application/json")
                .body(query)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get column information for a specific datasource
     */
    public JsonNode getColumnsForDatasource(String datasourceName) throws Exception {
        String columnsSql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT " +
                "FROM \"INFORMATION_SCHEMA\".\"COLUMNS\" " +
                "WHERE \"TABLE_SCHEMA\" = 'druid' AND \"TABLE_NAME\" = '" + datasourceName + "' " +
                "ORDER BY ORDINAL_POSITION";

        Map<String, Object> columnsQuery = new HashMap<>();
        columnsQuery.put("query", columnsSql);
        columnsQuery.put("resultFormat", "object");

        return druidRouterRestClient
                .post()
                .uri("/druid/v2/sql")
                .header("Content-Type", "application/json")
                .body(columnsQuery)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Build datasource information including column details
     */
    public Map<String, Object> buildDatasourceInfo(JsonNode datasource, String datasourceName) {
        Map<String, Object> datasourceInfo = new HashMap<>();
        datasourceInfo.put("datasource", objectMapper.convertValue(datasource, Map.class));

        try {
            JsonNode columns = getColumnsForDatasource(datasourceName);
            datasourceInfo.put("columns", objectMapper.convertValue(columns, List.class));
        } catch (Exception e) {
            datasourceInfo.put("columns", List.of());
            datasourceInfo.put("columns_error", "Failed to retrieve column information: " + e.getMessage());
        }

        return datasourceInfo;
    }

    /**
     * Kill a datasource permanently with specified interval
     */
    public JsonNode killDatasource(String datasourceName, String interval) throws RestClientException {
        return druidRouterRestClient
                .delete()
                .uri("/druid/coordinator/v1/datasources/{datasourceName}?kill=true&interval={interval}", datasourceName, interval)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}
