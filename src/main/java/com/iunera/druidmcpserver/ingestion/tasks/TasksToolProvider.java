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
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class TasksToolProvider {

    private final TasksRepository tasksRepository;
    private final ObjectMapper objectMapper;

    public TasksToolProvider(TasksRepository tasksRepository, ObjectMapper objectMapper) {
        this.tasksRepository = tasksRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Kill a task
     */
    @Tool(description = "Kill/shutdown a Druid task by task ID")
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

    /**
     * Get task raw details
     */
    @Tool(description = "Get raw details of a Druid task by task ID")
    public String getTaskRawDetails(String taskId) {
        try {
            JsonNode result = tasksRepository.getTaskDetails(taskId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting task raw details: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to get task raw details: %s", e.getMessage());
        }
    }

    /**
     * Get task ingestion spec
     */
    @Tool(description = "Get the ingestion specification of a Druid task by task ID")
    public String getTaskIngestionSpec(String taskId) {
        try {
            JsonNode result = tasksRepository.getTaskIngestionSpec(taskId);
            if (result == null) {
                return String.format("No ingestion spec found for task: %s", taskId);
            }
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting task ingestion spec: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to get task ingestion spec: %s", e.getMessage());
        }
    }

    /**
     * Get task reports
     */
    @Tool(description = "Get the reports of a Druid task by task ID")
    public String getTaskReports(String taskId) {
        try {
            JsonNode result = tasksRepository.getTaskReports(taskId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting task reports: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to get task reports: %s", e.getMessage());
        }
    }

    /**
     * Get task log
     */
    @Tool(description = "Get the log of a Druid task by task ID")
    public String getTaskLog(String taskId) {
        try {
            String result = tasksRepository.getTaskLog(taskId);
            return result;
        } catch (RestClientException e) {
            return String.format("Error getting task log: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to get task log: %s", e.getMessage());
        }
    }

    /**
     * Get task log with offset
     */
    @Tool(description = "Get the log of a Druid task by task ID starting from a specific offset")
    public String getTaskLogWithOffset(String taskId, long offset) {
        try {
            String result = tasksRepository.getTaskLog(taskId, offset);
            return result;
        } catch (RestClientException e) {
            return String.format("Error getting task log with offset: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to get task log with offset: %s", e.getMessage());
        }
    }

    /**
     * Get task status
     */
    @Tool(description = "Get the status of a Druid task by task ID")
    public String getTaskStatus(String taskId) {
        try {
            JsonNode result = tasksRepository.getTaskStatus(taskId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting task status: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to get task status: %s", e.getMessage());
        }
    }

    /**
     * List all running ingestion tasks
     */
    @Tool(description = "List all currently running Druid ingestion tasks")
    public String listRunningTasks() {
        try {
            JsonNode result = tasksRepository.getRunningTasks();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error listing running tasks: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to list running tasks: %s", e.getMessage());
        }
    }

    /**
     * List all pending ingestion tasks
     */
    @Tool(description = "List all pending Druid ingestion tasks")
    public String listPendingTasks() {
        try {
            JsonNode result = tasksRepository.getPendingTasks();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error listing pending tasks: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to list pending tasks: %s", e.getMessage());
        }
    }

    /**
     * List all waiting ingestion tasks
     */
    @Tool(description = "List all waiting Druid ingestion tasks")
    public String listWaitingTasks() {
        try {
            JsonNode result = tasksRepository.getWaitingTasks();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error listing waiting tasks: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to list waiting tasks: %s", e.getMessage());
        }
    }

    /**
     * List all completed ingestion tasks
     */
    @Tool(description = "List all completed Druid ingestion tasks")
    public String listCompletedTasks() {
        try {
            JsonNode result = tasksRepository.getCompleteTasks();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error listing completed tasks: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to list completed tasks: %s", e.getMessage());
        }
    }

}
