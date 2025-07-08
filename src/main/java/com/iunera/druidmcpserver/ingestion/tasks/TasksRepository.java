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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Repository
public class TasksRepository {

    private final RestClient druidRouterRestClient;

    public TasksRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;
    }

    /**
     * Kill/shutdown a task
     */
    public JsonNode killTask(String taskId) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/indexer/v1/task/{taskId}/shutdown", taskId)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get task details (raw task information)
     */
    public JsonNode getTaskDetails(String taskId) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/task/{taskId}", taskId)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get task ingestion spec
     */
    public JsonNode getTaskIngestionSpec(String taskId) throws RestClientException {
        // Get the full task details and extract the spec
        JsonNode taskDetails = getTaskDetails(taskId);
        return taskDetails.get("spec");
    }

    /**
     * Get task reports
     */
    public JsonNode getTaskReports(String taskId) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/task/{taskId}/reports", taskId)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get task log
     */
    public String getTaskLog(String taskId) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/task/{taskId}/log", taskId)
                .retrieve()
                .body(String.class);
    }

    /**
     * Get task log with offset
     */
    public String getTaskLog(String taskId, long offset) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/task/{taskId}/log?offset={offset}", taskId, offset)
                .retrieve()
                .body(String.class);
    }

    /**
     * Get task status
     */
    public JsonNode getTaskStatus(String taskId) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/task/{taskId}/status", taskId)
                .retrieve()
                .body(JsonNode.class);
    }


    /**
     * List all running tasks
     */
    public JsonNode getRunningTasks() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/runningTasks")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * List all pending tasks
     */
    public JsonNode getPendingTasks() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/pendingTasks")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * List all waiting tasks
     */
    public JsonNode getWaitingTasks() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/waitingTasks")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * List all complete tasks
     */
    public JsonNode getCompleteTasks() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/indexer/v1/completeTasks")
                .retrieve()
                .body(JsonNode.class);
    }

}
