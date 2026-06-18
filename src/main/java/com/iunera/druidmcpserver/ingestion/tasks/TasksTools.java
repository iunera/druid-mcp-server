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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class TasksTools {

    private final TasksRepository tasksRepository;
    private final ObjectMapper objectMapper;

    public TasksTools(TasksRepository tasksRepository, ObjectMapper objectMapper) {
        this.tasksRepository = tasksRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get tasks listing
     */
    @McpTool(description = "List ingestion tasks matching specific states. Parameters: [state] (Enum: RUNNING, PENDING, WAITING, COMPLETED, optional).")
    public String getTasks(
            @McpToolParam(description = "Task state to list: RUNNING, PENDING, WAITING, COMPLETED (optional)", required = false) String state
    ) {
        try {
            if (state == null || state.trim().isEmpty()) {
                JsonNode result = tasksRepository.getRunningTasks();
                return objectMapper.writeValueAsString(result);
            }
            switch (state.toUpperCase()) {
                case "RUNNING":
                    return objectMapper.writeValueAsString(tasksRepository.getRunningTasks());
                case "PENDING":
                    return objectMapper.writeValueAsString(tasksRepository.getPendingTasks());
                case "WAITING":
                    return objectMapper.writeValueAsString(tasksRepository.getWaitingTasks());
                case "COMPLETED":
                    return objectMapper.writeValueAsString(tasksRepository.getCompleteTasks());
                default:
                    return String.format("Error: Unsupported state '%s'. Supported: RUNNING, PENDING, WAITING, COMPLETED", state);
            }
        } catch (RestClientException e) {
            return String.format("Error getting tasks: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process tasks request: %s", e.getMessage());
        }
    }

    /**
     * Get detailed task information
     */
    @McpTool(description = "Fetch detailed information, specifications, execution reports, or execution logs for a task. Parameters: [taskId] (String, required), [aspect] (Enum: STATUS, RAW_DETAILS, SPEC, REPORTS, LOG, required), and [logOffset] (Long, optional) to begin reading task logs from a specific byte offset.")
    public String getTaskDetails(
            @McpToolParam(description = "ID of the task (required)", required = true) String taskId,
            @McpToolParam(description = "Aspect to retrieve: STATUS, RAW_DETAILS, SPEC, REPORTS, LOG (required)", required = true) String aspect,
            @McpToolParam(description = "Byte offset to start reading task logs (optional, used with aspect=LOG)", required = false) Long logOffset
    ) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                return "Error: [taskId] parameter is required";
            }
            if (aspect == null || aspect.trim().isEmpty()) {
                return "Error: [aspect] parameter is required";
            }
            switch (aspect.toUpperCase()) {
                case "STATUS":
                    return objectMapper.writeValueAsString(tasksRepository.getTaskStatus(taskId));
                case "RAW_DETAILS":
                    return objectMapper.writeValueAsString(tasksRepository.getTaskDetails(taskId));
                case "SPEC":
                    JsonNode spec = tasksRepository.getTaskIngestionSpec(taskId);
                    return spec != null ? objectMapper.writeValueAsString(spec) : String.format("No ingestion spec found for task: %s", taskId);
                case "REPORTS":
                    return objectMapper.writeValueAsString(tasksRepository.getTaskReports(taskId));
                case "LOG":
                    if (logOffset != null) {
                        return tasksRepository.getTaskLog(taskId, logOffset);
                    }
                    return tasksRepository.getTaskLog(taskId);
                default:
                    return String.format("Error: Unsupported aspect '%s'. Supported: STATUS, RAW_DETAILS, SPEC, REPORTS, LOG", aspect);
            }
        } catch (RestClientException e) {
            return String.format("Error getting task details for '%s' (aspect: %s): %s", taskId, aspect, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process task details request: %s", e.getMessage());
        }
    }

    /**
     * Shutdown a task
     */
    @McpTool(description = "Kill/shutdown a Druid task. Parameters: [taskId] (String, required).")
    public String shutdownTask(
            @McpToolParam(description = "ID of the task to shutdown (required)", required = true) String taskId
    ) {
        try {
            JsonNode result = tasksRepository.killTask(taskId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error shutting down task: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to shut down task: %s", e.getMessage());
        }
    }
}
