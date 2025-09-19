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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
@Component
public class MsqQueryTools {

    private final QueryRepository queryRepository;
    private final TasksRepository tasksRepository;
    private final ObjectMapper objectMapper;

    public MsqQueryTools(QueryRepository queryRepository,
                         TasksRepository tasksRepository,
                         ObjectMapper objectMapper) {
        this.queryRepository = queryRepository;
        this.tasksRepository = tasksRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Execute a multi-stage Druid SQL query as a task
     */
    @McpTool(description = "Execute a multi-stage SQL query against Druid datasources as a task. This is suitable for complex queries, large datasets, and INSERT/REPLACE operations. Provide the SQL query as a parameter.")
    public String queryDruidMultiStage(String sqlQuery) {
        try {
            JsonNode result = queryRepository.executeMultiStageSqlQuery(sqlQuery);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error executing multi-stage SQL query '%s': %s", sqlQuery, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process multi-stage query response for '%s': %s", sqlQuery, e.getMessage());
        }
    }

    /**
     * Execute a multi-stage Druid SQL query with custom context parameters
     */
    @McpTool(description = "Execute a multi-stage SQL query with custom context parameters. Provide the SQL query and context as JSON string. Context can include maxNumTasks, taskAssignment, finalizeAggregations, groupByEnableMultiValueUnnesting, etc.")
    public String queryDruidMultiStageWithContext(String sqlQuery, String contextJson) {
        try {
            Map<String, Object> context = null;

            // Parse context JSON if provided
            if (contextJson != null && !contextJson.trim().isEmpty()) {
                context = objectMapper.readValue(contextJson,
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            }

            JsonNode result = queryRepository.executeMultiStageSqlQueryWithContext(sqlQuery, context);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error executing multi-stage SQL query with context '%s': %s", sqlQuery, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process multi-stage query with context response for '%s': %s", sqlQuery, e.getMessage());
        }
    }

    /**
     * Get status of a multi-stage query task
     */
    @McpTool(description = "Get the status of a multi-stage query task. Provide the task ID as a parameter.")
    public String getMultiStageQueryTaskStatus(String taskId) {
        try {
            JsonNode result = tasksRepository.getTaskStatus(taskId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving task status for task ID '%s': %s", taskId, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process task status response for task ID '%s': %s", taskId, e.getMessage());
        }
    }

    /**
     * Cancel a multi-stage query task
     */
    @McpTool(description = "Cancel a running multi-stage query task. Provide the task ID as a parameter.")
    public String cancelMultiStageQueryTask(String taskId) {
        try {
            JsonNode result = tasksRepository.killTask(taskId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error cancelling task with ID '%s': %s", taskId, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process task cancellation response for task ID '%s': %s", taskId, e.getMessage());
        }
    }
}