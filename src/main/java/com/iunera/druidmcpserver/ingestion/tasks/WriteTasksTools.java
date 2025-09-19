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

package com.iunera.druidmcpserver.ingestion.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
@Component
public class WriteTasksTools {

    private final TasksRepository tasksRepository;
    private final ObjectMapper objectMapper;

    public WriteTasksTools(TasksRepository tasksRepository, ObjectMapper objectMapper) {
        this.tasksRepository = tasksRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Kill a task
     */
    @McpTool(description = "Kill/shutdown a Druid task by task ID")
    public String killTask(String taskId) {
        try {
            JsonNode result = tasksRepository.killTask(taskId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error killing task: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to kill task: %s", e.getMessage());
        }
    }
}
