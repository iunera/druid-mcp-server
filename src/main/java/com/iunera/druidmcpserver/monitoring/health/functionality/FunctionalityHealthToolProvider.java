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

package com.iunera.druidmcpserver.monitoring.health.functionality;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.iunera.druidmcpserver.datamanagement.segments.SegmentRepository;
import com.iunera.druidmcpserver.ingestion.supervisors.SupervisorsRepository;
import com.iunera.druidmcpserver.ingestion.tasks.TasksRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * Functionality Health Tool Provider - Monitors the health of specific Druid functionalities
 * like supervisors and historicals, providing targeted health assessments beyond basic status checks
 */
@Component
public class FunctionalityHealthToolProvider {

    private final SupervisorsRepository supervisorsRepository;
    private final SegmentRepository segmentRepository;
    private final TasksRepository tasksRepository;
    private final HealthStatusRepository healthStatusRepository;
    private final ObjectMapper objectMapper;

    public FunctionalityHealthToolProvider(SupervisorsRepository supervisorsRepository,
                                           SegmentRepository segmentRepository,
                                           TasksRepository tasksRepository,
                                           HealthStatusRepository healthStatusRepository,
                                           ObjectMapper objectMapper) {
        this.supervisorsRepository = supervisorsRepository;
        this.segmentRepository = segmentRepository;
        this.tasksRepository = tasksRepository;
        this.healthStatusRepository = healthStatusRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Check the health of specific or all Druid component functionalities.
     */
    @McpTool(description = "Validate operations of ingestion systems, supervisor state transitions, and historical query latency. Parameters: [component] (Enum: ALL, SUPERVISORS, HISTORICALS, INGESTION, optional), and [quick] (Boolean, optional) to trigger a rapid smoke check.")
    public String checkFunctionalityHealth(
            @McpToolParam(description = "The component to check: ALL, SUPERVISORS, HISTORICALS, INGESTION (optional, defaults to ALL)", required = false) String component,
            @McpToolParam(description = "Whether to trigger a quick rapid smoke check (optional, defaults to false)", required = false) Boolean quick
    ) {
        boolean isQuick = quick != null && quick;
        if (isQuick) {
            return runQuickFunctionalityCheck();
        }

        String comp = component == null ? "ALL" : component.toUpperCase();
        switch (comp) {
            case "SUPERVISORS":
                return runCheckSupervisorHealth();
            case "HISTORICALS":
                return runCheckHistoricalHealth();
            case "INGESTION":
                return runCheckIngestionHealth();
            case "ALL":
            default:
                return runCheckComprehensiveFunctionalityHealth();
        }
    }

    private String runCheckSupervisorHealth() {
        var healthReport = objectMapper.createObjectNode();
        var issues = objectMapper.createArrayNode();
        var recommendations = objectMapper.createArrayNode();
        var supervisorDetails = objectMapper.createArrayNode();

        try {
            // Get all supervisors
            JsonNode supervisors = supervisorsRepository.getAllSupervisors();

            if (supervisors == null || !supervisors.isArray() || supervisors.size() == 0) {
                issues.add("No supervisors found in the cluster");
                recommendations.add("Check if any ingestion supervisors are configured and running");
                healthReport.put("supervisor_count", 0);
                healthReport.put("health_status", "WARNING");
            } else {
                healthReport.put("supervisor_count", supervisors.size());

                int healthySupervisors = 0;
                int unhealthySupervisors = 0;

                // Check each supervisor's health
                for (JsonNode supervisor : supervisors) {
                    var supervisorHealth = objectMapper.createObjectNode();
                    String supervisorId = supervisor.asText();
                    supervisorHealth.put("supervisor_id", supervisorId);

                    try {
                        // Get detailed supervisor status
                        JsonNode status = supervisorsRepository.getSupervisorStatus(supervisorId);
                        supervisorHealth.set("status", status);

                        // Analyze supervisor health
                        String state = status.path("payload").path("state").asText();
                        supervisorHealth.put("state", state);

                        if ("RUNNING".equals(state)) {
                            healthySupervisors++;
                            supervisorHealth.put("health", "HEALTHY");
                        } else if ("SUSPENDED".equals(state)) {
                            supervisorHealth.put("health", "SUSPENDED");
                            issues.add("Supervisor " + supervisorId + " is suspended");
                            recommendations.add("Check if supervisor " + supervisorId + " should be resumed");
                        } else {
                            unhealthySupervisors++;
                            supervisorHealth.put("health", "UNHEALTHY");
                            issues.add("Supervisor " + supervisorId + " is in state: " + state);
                            recommendations.add("Investigate supervisor " + supervisorId + " issues");
                        }

                        // Check for ingestion lag
                        JsonNode payload = status.path("payload");
                        if (payload.has("lag")) {
                            long lag = payload.path("lag").asLong();
                            supervisorHealth.put("lag", lag);
                            if (lag > 300000) { // 5 minutes
                                issues.add("Supervisor " + supervisorId + " has high ingestion lag: " + lag + "ms");
                                recommendations.add("Check data source and ingestion performance for " + supervisorId);
                            }
                        }

                    } catch (Exception e) {
                        unhealthySupervisors++;
                        supervisorHealth.put("health", "ERROR");
                        supervisorHealth.put("error", e.getMessage());
                        issues.add("Failed to get status for supervisor " + supervisorId + ": " + e.getMessage());
                        recommendations.add("Check connectivity and supervisor " + supervisorId + " configuration");
                    }

                    supervisorDetails.add(supervisorHealth);
                }

                healthReport.put("healthy_supervisors", healthySupervisors);
                healthReport.put("unhealthy_supervisors", unhealthySupervisors);

                // Determine overall health status
                if (unhealthySupervisors == 0) {
                    healthReport.put("health_status", "HEALTHY");
                } else if (healthySupervisors > unhealthySupervisors) {
                    healthReport.put("health_status", "WARNING");
                } else {
                    healthReport.put("health_status", "CRITICAL");
                }
            }

        } catch (Exception e) {
            issues.add("Failed to retrieve supervisors: " + e.getMessage());
            recommendations.add("Check Druid coordinator connectivity and supervisor API availability");
            healthReport.put("supervisor_count", 0);
            healthReport.put("health_status", "ERROR");
        }

        healthReport.set("supervisor_details", supervisorDetails);
        healthReport.set("issues", issues);
        healthReport.set("recommendations", recommendations);
        healthReport.put("timestamp", System.currentTimeMillis());

        return healthReport.toString();
    }

    private String runCheckHistoricalHealth() {
        var healthReport = objectMapper.createObjectNode();
        var issues = objectMapper.createArrayNode();
        var recommendations = objectMapper.createArrayNode();

        try {
            // Get segment information
            JsonNode segments = segmentRepository.getAllSegments();
            if (segments != null && segments.isArray()) {
                healthReport.put("total_segments", segments.size());

                // Analyze segment distribution
                var segmentsByDatasource = objectMapper.createObjectNode();
                var segmentsByServer = objectMapper.createObjectNode();

                for (JsonNode segment : segments) {
                    String datasource = segment.path("dataSource").asText();
                    String server = segment.path("server").asText();

                    // Count by datasource
                    int dsCount = segmentsByDatasource.path(datasource).asInt(0);
                    segmentsByDatasource.put(datasource, dsCount + 1);

                    // Count by server
                    int serverCount = segmentsByServer.path(server).asInt(0);
                    segmentsByServer.put(server, serverCount + 1);
                }

                healthReport.set("segments_by_datasource", segmentsByDatasource);
                healthReport.set("segments_by_server", segmentsByServer);

                // Check for uneven distribution
                if (segmentsByServer.size() > 1) {
                    final int[] maxSegments = {0};
                    final int[] minSegments = {Integer.MAX_VALUE};
                    segmentsByServer.properties().forEach(entry -> {
                        int count = entry.getValue().asInt();
                        if (count > maxSegments[0]) maxSegments[0] = count;
                        if (count < minSegments[0]) minSegments[0] = count;
                    });

                    double imbalanceRatio = (double) maxSegments[0] / minSegments[0];
                    if (imbalanceRatio > 2.0) {
                        issues.add("Segment distribution is uneven across historical nodes (ratio: " + String.format("%.2f", imbalanceRatio) + ")");
                        recommendations.add("Consider rebalancing segments across historical nodes");
                    }
                }
            } else {
                issues.add("No segments found or unable to retrieve segment information");
                recommendations.add("Check if data has been ingested and segments are available");
                healthReport.put("total_segments", 0);
            }

            // Check coordinator health for load queue information
            try {
                JsonNode coordinatorHealth = healthStatusRepository.getCoordinatorHealth();
                healthReport.set("coordinator_health", coordinatorHealth);
            } catch (Exception e) {
                issues.add("Unable to get coordinator health: " + e.getMessage());
                recommendations.add("Check coordinator connectivity and status");
            }

            // Determine overall health status
            if (issues.size() == 0) {
                healthReport.put("health_status", "HEALTHY");
            } else if (issues.size() <= 2) {
                healthReport.put("health_status", "WARNING");
            } else {
                healthReport.put("health_status", "CRITICAL");
            }

        } catch (Exception e) {
            issues.add("Failed to analyze historical health: " + e.getMessage());
            recommendations.add("Check Druid cluster connectivity and API availability");
            healthReport.put("total_segments", 0);
            healthReport.set("segments_by_datasource", objectMapper.createObjectNode());
            healthReport.set("segments_by_server", objectMapper.createObjectNode());
            healthReport.put("health_status", "ERROR");
        }

        healthReport.set("issues", issues);
        healthReport.set("recommendations", recommendations);
        healthReport.put("timestamp", System.currentTimeMillis());

        return healthReport.toString();
    }

    private String runCheckIngestionHealth() {
        var healthReport = objectMapper.createObjectNode();
        var issues = objectMapper.createArrayNode();
        var recommendations = objectMapper.createArrayNode();

        try {
            JsonNode runningTasks = tasksRepository.getRunningTasks();
            JsonNode pendingTasks = tasksRepository.getPendingTasks();
            JsonNode completeTasks = tasksRepository.getCompleteTasks();

            var taskSummary = objectMapper.createObjectNode();
            taskSummary.put("running", runningTasks != null && runningTasks.isArray() ? runningTasks.size() : 0);
            taskSummary.put("pending", pendingTasks != null && pendingTasks.isArray() ? pendingTasks.size() : 0);
            taskSummary.put("complete", completeTasks != null && completeTasks.isArray() ? completeTasks.size() : 0);

            healthReport.set("task_summary", taskSummary);

            if (pendingTasks != null && pendingTasks.isArray() && pendingTasks.size() > 10) {
                issues.add("High number of pending tasks: " + pendingTasks.size());
                recommendations.add("Check task execution capacity and resource availability");
                healthReport.put("health_status", "WARNING");
            } else {
                healthReport.put("health_status", "HEALTHY");
            }

        } catch (Exception e) {
            issues.add("Failed to get task information: " + e.getMessage());
            recommendations.add("Check task API connectivity and permissions");
            healthReport.put("health_status", "ERROR");
        }

        healthReport.set("issues", issues);
        healthReport.set("recommendations", recommendations);
        healthReport.put("timestamp", System.currentTimeMillis());

        return healthReport.toString();
    }

    private String runCheckComprehensiveFunctionalityHealth() {
        var healthReport = objectMapper.createObjectNode();
        var overallIssues = objectMapper.createArrayNode();
        var overallRecommendations = objectMapper.createArrayNode();

        try {
            // Check supervisor health
            String supervisorHealthStr = runCheckSupervisorHealth();
            JsonNode supervisorHealth = objectMapper.readTree(supervisorHealthStr);
            healthReport.set("supervisor_health", supervisorHealth);

            // Collect supervisor issues
            if (supervisorHealth.has("issues")) {
                supervisorHealth.get("issues").forEach(overallIssues::add);
            }
            if (supervisorHealth.has("recommendations")) {
                supervisorHealth.get("recommendations").forEach(overallRecommendations::add);
            }

            // Check historical health
            String historicalHealthStr = runCheckHistoricalHealth();
            JsonNode historicalHealth = objectMapper.readTree(historicalHealthStr);
            healthReport.set("historical_health", historicalHealth);

            // Collect historical issues
            if (historicalHealth.has("issues")) {
                historicalHealth.get("issues").forEach(overallIssues::add);
            }
            if (historicalHealth.has("recommendations")) {
                historicalHealth.get("recommendations").forEach(overallRecommendations::add);
            }

            // Check task health
            try {
                JsonNode runningTasks = tasksRepository.getRunningTasks();
                JsonNode pendingTasks = tasksRepository.getPendingTasks();
                JsonNode completeTasks = tasksRepository.getCompleteTasks();

                var taskSummary = objectMapper.createObjectNode();
                taskSummary.put("running", runningTasks != null && runningTasks.isArray() ? runningTasks.size() : 0);
                taskSummary.put("pending", pendingTasks != null && pendingTasks.isArray() ? pendingTasks.size() : 0);
                taskSummary.put("complete", completeTasks != null && completeTasks.isArray() ? completeTasks.size() : 0);

                healthReport.set("task_summary", taskSummary);

                // Check for stuck tasks
                if (pendingTasks != null && pendingTasks.isArray() && pendingTasks.size() > 10) {
                    overallIssues.add("High number of pending tasks: " + pendingTasks.size());
                    overallRecommendations.add("Check task execution capacity and resource availability");
                }

            } catch (Exception e) {
                overallIssues.add("Failed to get task information: " + e.getMessage());
                overallRecommendations.add("Check task API connectivity and permissions");
                // Set default task summary when task information retrieval fails
                var taskSummary = objectMapper.createObjectNode();
                taskSummary.put("running", 0);
                taskSummary.put("pending", 0);
                taskSummary.put("complete", 0);
                healthReport.set("task_summary", taskSummary);
            }

            // Determine overall health status
            String supervisorStatus = supervisorHealth.path("health_status").asText();
            String historicalStatus = historicalHealth.path("health_status").asText();

            String overallStatus;
            if ("ERROR".equals(supervisorStatus) || "ERROR".equals(historicalStatus)) {
                overallStatus = "ERROR";
            } else if ("CRITICAL".equals(supervisorStatus) || "CRITICAL".equals(historicalStatus)) {
                overallStatus = "CRITICAL";
            } else if ("WARNING".equals(supervisorStatus) || "WARNING".equals(historicalStatus)) {
                overallStatus = "WARNING";
            } else {
                overallStatus = "HEALTHY";
            }

            healthReport.put("overall_health_status", overallStatus);

        } catch (Exception e) {
            overallIssues.add("Failed to perform comprehensive functionality health check: " + e.getMessage());
            overallRecommendations.add("Check overall Druid cluster connectivity and API availability");
            healthReport.put("overall_health_status", "ERROR");
        }

        healthReport.set("overall_issues", overallIssues);
        healthReport.set("overall_recommendations", overallRecommendations);
        healthReport.put("timestamp", System.currentTimeMillis());

        return healthReport.toString();
    }

    private String runQuickFunctionalityCheck() {
        var healthReport = objectMapper.createObjectNode();
        var issues = objectMapper.createArrayNode();

        try {
            // Quick supervisor check
            JsonNode supervisors = supervisorsRepository.getAllSupervisors();
            int supervisorCount = supervisors != null && supervisors.isArray() ? supervisors.size() : 0;
            healthReport.put("supervisor_count", supervisorCount);

            // Quick segment check
            JsonNode segments = segmentRepository.getAllSegments();
            int segmentCount = segments != null && segments.isArray() ? segments.size() : 0;
            healthReport.put("segment_count", segmentCount);

            // Quick task check
            JsonNode runningTasks = tasksRepository.getRunningTasks();
            int runningTaskCount = runningTasks != null && runningTasks.isArray() ? runningTasks.size() : 0;
            healthReport.put("running_task_count", runningTaskCount);

            // Quick health assessment
            if (supervisorCount == 0) {
                issues.add("No supervisors found");
            }
            if (segmentCount == 0) {
                issues.add("No segments found");
            }

            String status;
            if (issues.size() == 0) {
                status = "HEALTHY";
            } else if (issues.size() == 1) {
                status = "WARNING";
            } else {
                status = "CRITICAL";
            }

            healthReport.put("quick_health_status", status);

        } catch (Exception e) {
            issues.add("Failed to perform quick functionality check: " + e.getMessage());
            healthReport.put("supervisor_count", 0);
            healthReport.put("segment_count", 0);
            healthReport.put("running_task_count", 0);
            healthReport.put("quick_health_status", "ERROR");
        }

        healthReport.set("issues", issues);
        healthReport.put("timestamp", System.currentTimeMillis());

        return healthReport.toString();
    }
}
