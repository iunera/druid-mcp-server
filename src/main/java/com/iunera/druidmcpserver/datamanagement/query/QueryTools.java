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
import com.iunera.druidmcpserver.ingestion.tasks.TasksRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class QueryTools {

    private final QueryRepository queryRepository;
    private final TasksRepository tasksRepository;
    private final ObjectMapper objectMapper;

    public QueryTools(QueryRepository queryRepository,
                      TasksRepository tasksRepository,
                      ObjectMapper objectMapper) {
        this.queryRepository = queryRepository;
        this.tasksRepository = tasksRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Execute a Druid SQL query against a datasource
     */
    @McpTool(description = "Execute a SQL query against Druid datasources. Provide the SQL query as a parameter.")
    public String queryDruidSql(String sqlQuery) {
        try {
            JsonNode result = queryRepository.executeSqlQuery(sqlQuery);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error executing SQL query '%s': %s", sqlQuery, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process query response for '%s': %s", sqlQuery, e.getMessage());
        }
    }
}