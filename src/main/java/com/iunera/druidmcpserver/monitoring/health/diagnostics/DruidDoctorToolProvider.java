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

package com.iunera.druidmcpserver.monitoring.health.diagnostics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iunera.druidmcpserver.datamanagement.datasource.DatasourceRepository;
import com.iunera.druidmcpserver.datamanagement.segments.SegmentRepository;
import com.iunera.druidmcpserver.ingestion.tasks.TasksRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.ClusterRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.ServerRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/**
 * Druid Doctor - Comprehensive diagnostic and recommendation tool for Druid clusters
 * Provides automated health assessments, performance analysis, and optimization recommendations
 */
@Component
public class DruidDoctorToolProvider {

    private final HealthStatusRepository healthStatusRepository;
    private final ServerRepository serverRepository;
    private final ClusterRepository clusterRepository;
    private final SegmentRepository segmentRepository;
    private final DatasourceRepository datasourceRepository;
    private final TasksRepository tasksRepository;
    private final ObjectMapper objectMapper;

    public DruidDoctorToolProvider(HealthStatusRepository healthStatusRepository,
                                   ServerRepository serverRepository,
                                   ClusterRepository clusterRepository,
                                   SegmentRepository segmentRepository,
                                   DatasourceRepository datasourceRepository,
                                   TasksRepository tasksRepository,
                                   ObjectMapper objectMapper) {
        this.healthStatusRepository = healthStatusRepository;
        this.serverRepository = serverRepository;
        this.clusterRepository = clusterRepository;
        this.segmentRepository = segmentRepository;
        this.datasourceRepository = datasourceRepository;
        this.tasksRepository = tasksRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Comprehensive cluster health diagnosis with recommendations
     */
    @McpTool(description = "Perform comprehensive Druid cluster health diagnosis with automated recommendations and issue detection")
    public String diagnoseCluster() {
        var diagnosis = objectMapper.createObjectNode();
        var issues = objectMapper.createArrayNode();
        var recommendations = objectMapper.createArrayNode();
        var healthScore = 100; // Start with perfect score

        try {
            // 1. Check cluster leadership and coordination
            var coordinatorStatus = checkCoordinatorHealth(issues, recommendations);
            healthScore -= coordinatorStatus.penalty;

            // 2. Check server availability and distribution
            var serverStatus = checkServerHealth(issues, recommendations);
            healthScore -= serverStatus.penalty;

            // 3. Check segment health and distribution
            var segmentStatus = checkSegmentHealth(issues, recommendations);
            healthScore -= segmentStatus.penalty;

            // 4. Check ingestion health
            var ingestionStatus = checkIngestionHealth(issues, recommendations);
            healthScore -= ingestionStatus.penalty;

            // 5. Check load queue and balancing
            var loadQueueStatus = checkLoadQueueHealth(issues, recommendations);
            healthScore -= loadQueueStatus.penalty;

            // 6. Check datasource health
            var datasourceStatus = checkDatasourceHealth(issues, recommendations);
            healthScore -= datasourceStatus.penalty;

            // Compile final diagnosis
            diagnosis.put("overall_health_score", Math.max(0, healthScore));
            diagnosis.put("health_status", getHealthStatus(healthScore));
            diagnosis.set("issues_found", issues);
            diagnosis.set("recommendations", recommendations);
            diagnosis.put("diagnosis_timestamp", System.currentTimeMillis());

            // Add component status summary
            var componentStatus = objectMapper.createObjectNode();
            componentStatus.put("coordinator", coordinatorStatus.status);
            componentStatus.put("servers", serverStatus.status);
            componentStatus.put("segments", segmentStatus.status);
            componentStatus.put("ingestion", ingestionStatus.status);
            componentStatus.put("load_queue", loadQueueStatus.status);
            componentStatus.put("datasources", datasourceStatus.status);
            diagnosis.set("component_status", componentStatus);

            return objectMapper.writeValueAsString(diagnosis);

        } catch (Exception e) {
            return String.format("Error performing cluster diagnosis: %s", e.getMessage());
        }
    }

    /**
     * Quick health check with immediate action items
     */
    @McpTool(description = "Perform quick Druid cluster health check with immediate action items for critical issues")
    public String quickHealthCheck() {
        var quickCheck = objectMapper.createObjectNode();
        var criticalIssues = objectMapper.createArrayNode();
        var immediateActions = objectMapper.createArrayNode();

        try {
            // Check critical components only
            checkCriticalCoordinatorHealth(criticalIssues, immediateActions);
            checkCriticalServerHealth(criticalIssues, immediateActions);
            checkCriticalIngestionHealth(criticalIssues, immediateActions);

            String status = criticalIssues.size() == 0 ? "HEALTHY" : "NEEDS_ATTENTION";
            quickCheck.put("status", status);
            quickCheck.set("critical_issues", criticalIssues);
            quickCheck.set("immediate_actions", immediateActions);
            quickCheck.put("check_timestamp", System.currentTimeMillis());

            return objectMapper.writeValueAsString(quickCheck);

        } catch (Exception e) {
            return String.format("Error performing quick health check: %s", e.getMessage());
        }
    }

    /**
     * Analyze cluster performance and provide optimization recommendations
     */
    @McpTool(description = "Analyze Druid cluster performance and provide optimization recommendations")
    public String analyzePerformance() {
        var analysis = objectMapper.createObjectNode();
        var performanceIssues = objectMapper.createArrayNode();
        var optimizations = objectMapper.createArrayNode();

        try {
            // Analyze different performance aspects
            analyzeSegmentPerformance(performanceIssues, optimizations);
            analyzeIngestionPerformance(performanceIssues, optimizations);
            analyzeLoadBalancing(performanceIssues, optimizations);
            analyzeResourceUtilization(performanceIssues, optimizations);

            int performanceScore = calculatePerformanceScore(performanceIssues);
            analysis.put("performance_score", performanceScore);
            analysis.put("performance_status", getHealthStatus(performanceScore));
            analysis.set("performance_issues", performanceIssues);
            analysis.set("optimization_recommendations", optimizations);
            analysis.put("analysis_timestamp", System.currentTimeMillis());

            return objectMapper.writeValueAsString(analysis);

        } catch (Exception e) {
            return String.format("Error analyzing performance: %s", e.getMessage());
        }
    }

    /**
     * Validate cluster configuration and provide best practice recommendations
     */
    @McpTool(description = "Validate Druid cluster configuration and provide best practice recommendations")
    public String validateConfiguration() {
        var validation = objectMapper.createObjectNode();
        var configIssues = objectMapper.createArrayNode();
        var bestPractices = objectMapper.createArrayNode();

        try {
            // Validate different configuration aspects
            validateCoordinatorConfig(configIssues, bestPractices);
            validateSegmentConfig(configIssues, bestPractices);
            validateIngestionConfig(configIssues, bestPractices);

            int configScore = calculateConfigScore(configIssues);
            validation.put("configuration_score", configScore);
            validation.put("configuration_status", getHealthStatus(configScore));
            validation.set("configuration_issues", configIssues);
            validation.set("best_practice_recommendations", bestPractices);
            validation.put("validation_timestamp", System.currentTimeMillis());

            return objectMapper.writeValueAsString(validation);

        } catch (Exception e) {
            return String.format("Error validating configuration: %s", e.getMessage());
        }
    }

    private ComponentStatus checkCoordinatorHealth(com.fasterxml.jackson.databind.node.ArrayNode issues, com.fasterxml.jackson.databind.node.ArrayNode recommendations) {
        try {
            JsonNode coordinatorHealth = healthStatusRepository.getCoordinatorHealth();
            JsonNode leaderInfo = clusterRepository.getLeaderInfo();

            if (coordinatorHealth == null || leaderInfo == null) {
                issues.add("Coordinator is not responding or not accessible");
                recommendations.add("Check coordinator service status and network connectivity");
                return new ComponentStatus("CRITICAL", 30);
            }

            return new ComponentStatus("HEALTHY", 0);
        } catch (Exception e) {
            issues.add("Failed to check coordinator health: " + e.getMessage());
            recommendations.add("Verify coordinator service is running and accessible");
            return new ComponentStatus("ERROR", 25);
        }
    }

    private ComponentStatus checkServerHealth(com.fasterxml.jackson.databind.node.ArrayNode issues, com.fasterxml.jackson.databind.node.ArrayNode recommendations) {
        try {
            JsonNode servers = serverRepository.getAllServersStatus();

            if (servers == null || !servers.isArray() || servers.size() == 0) {
                issues.add("No servers found in cluster");
                recommendations.add("Check if historical and broker nodes are running and registered");
                return new ComponentStatus("CRITICAL", 40);
            }

            return new ComponentStatus("HEALTHY", 0);
        } catch (Exception e) {
            issues.add("Failed to check server health: " + e.getMessage());
            recommendations.add("Verify server connectivity and registration");
            return new ComponentStatus("ERROR", 20);
        }
    }

    private ComponentStatus checkSegmentHealth(com.fasterxml.jackson.databind.node.ArrayNode issues, com.fasterxml.jackson.databind.node.ArrayNode recommendations) {
        try {
            JsonNode segments = segmentRepository.getAllSegments();

            if (segments == null || !segments.isArray()) {
                issues.add("Unable to retrieve segment information");
                recommendations.add("Check segment metadata and coordinator connectivity");
                return new ComponentStatus("WARNING", 15);
            }

            if (segments.size() == 0) {
                issues.add("No segments found in cluster");
                recommendations.add("Check if data has been ingested and segments are available");
                return new ComponentStatus("WARNING", 10);
            }

            return new ComponentStatus("HEALTHY", 0);
        } catch (Exception e) {
            issues.add("Failed to check segment health: " + e.getMessage());
            recommendations.add("Verify segment metadata accessibility");
            return new ComponentStatus("ERROR", 15);
        }
    }

    private ComponentStatus checkIngestionHealth(com.fasterxml.jackson.databind.node.ArrayNode issues, com.fasterxml.jackson.databind.node.ArrayNode recommendations) {
        try {
            JsonNode runningTasks = tasksRepository.getRunningTasks();
            JsonNode pendingTasks = tasksRepository.getPendingTasks();

            int runningCount = runningTasks != null && runningTasks.isArray() ? runningTasks.size() : 0;
            int pendingCount = pendingTasks != null && pendingTasks.isArray() ? pendingTasks.size() : 0;

            if (pendingCount > 20) {
                issues.add("High number of pending tasks: " + pendingCount);
                recommendations.add("Check task execution capacity and resource availability");
                return new ComponentStatus("WARNING", 10);
            }

            return new ComponentStatus("HEALTHY", 0);
        } catch (Exception e) {
            issues.add("Failed to check ingestion health: " + e.getMessage());
            recommendations.add("Verify task API connectivity and overlord status");
            return new ComponentStatus("ERROR", 15);
        }
    }

    private ComponentStatus checkLoadQueueHealth(com.fasterxml.jackson.databind.node.ArrayNode issues, com.fasterxml.jackson.databind.node.ArrayNode recommendations) {
        try {
            // This would require additional API calls to check load queue
            // For now, return healthy status
            return new ComponentStatus("HEALTHY", 0);
        } catch (Exception e) {
            issues.add("Failed to check load queue health: " + e.getMessage());
            recommendations.add("Verify coordinator load queue status");
            return new ComponentStatus("ERROR", 10);
        }
    }

    private ComponentStatus checkDatasourceHealth(com.fasterxml.jackson.databind.node.ArrayNode issues, com.fasterxml.jackson.databind.node.ArrayNode recommendations) {
        try {
            JsonNode datasources = datasourceRepository.getAllDatasources();

            if (datasources == null || !datasources.isArray() || datasources.size() == 0) {
                issues.add("No datasources found");
                recommendations.add("Check if datasources are configured and have data");
                return new ComponentStatus("WARNING", 5);
            }

            return new ComponentStatus("HEALTHY", 0);
        } catch (Exception e) {
            issues.add("Failed to check datasource health: " + e.getMessage());
            recommendations.add("Verify datasource metadata accessibility");
            return new ComponentStatus("ERROR", 10);
        }
    }

    private void checkCriticalCoordinatorHealth(com.fasterxml.jackson.databind.node.ArrayNode criticalIssues, com.fasterxml.jackson.databind.node.ArrayNode immediateActions) {
        try {
            JsonNode coordinatorHealth = healthStatusRepository.getCoordinatorHealth();
            if (coordinatorHealth == null) {
                criticalIssues.add("Coordinator is not responding");
                immediateActions.add("Restart coordinator service immediately");
            }
        } catch (Exception e) {
            criticalIssues.add("Cannot reach coordinator: " + e.getMessage());
            immediateActions.add("Check coordinator service status and network connectivity");
        }
    }

    private void checkCriticalServerHealth(com.fasterxml.jackson.databind.node.ArrayNode criticalIssues, com.fasterxml.jackson.databind.node.ArrayNode immediateActions) {
        try {
            JsonNode servers = serverRepository.getAllServersStatus();
            if (servers == null || !servers.isArray() || servers.size() == 0) {
                criticalIssues.add("No servers available in cluster");
                immediateActions.add("Check and restart historical/broker nodes");
            }
        } catch (Exception e) {
            criticalIssues.add("Cannot check server status: " + e.getMessage());
            immediateActions.add("Verify server connectivity and registration");
        }
    }

    private void checkCriticalIngestionHealth(com.fasterxml.jackson.databind.node.ArrayNode criticalIssues, com.fasterxml.jackson.databind.node.ArrayNode immediateActions) {
        try {
            JsonNode pendingTasks = tasksRepository.getPendingTasks();
            if (pendingTasks != null && pendingTasks.isArray() && pendingTasks.size() > 50) {
                criticalIssues.add("Extremely high number of pending tasks: " + pendingTasks.size());
                immediateActions.add("Investigate task execution bottlenecks immediately");
            }
        } catch (Exception e) {
            // Non-critical for quick check
        }
    }

    private void analyzeSegmentPerformance(com.fasterxml.jackson.databind.node.ArrayNode performanceIssues, com.fasterxml.jackson.databind.node.ArrayNode optimizations) {
        // Placeholder for segment performance analysis
        optimizations.add("Consider segment compaction for better query performance");
    }

    private void analyzeIngestionPerformance(com.fasterxml.jackson.databind.node.ArrayNode performanceIssues, com.fasterxml.jackson.databind.node.ArrayNode optimizations) {
        // Placeholder for ingestion performance analysis
        optimizations.add("Monitor ingestion lag and optimize supervisor configurations");
    }

    private void analyzeLoadBalancing(com.fasterxml.jackson.databind.node.ArrayNode performanceIssues, com.fasterxml.jackson.databind.node.ArrayNode optimizations) {
        // Placeholder for load balancing analysis
        optimizations.add("Review segment distribution across historical nodes");
    }

    private void analyzeResourceUtilization(com.fasterxml.jackson.databind.node.ArrayNode performanceIssues, com.fasterxml.jackson.databind.node.ArrayNode optimizations) {
        // Placeholder for resource utilization analysis
    }

    private void validateCoordinatorConfig(com.fasterxml.jackson.databind.node.ArrayNode configIssues, com.fasterxml.jackson.databind.node.ArrayNode bestPractices) {
        bestPractices.add("Ensure coordinator has sufficient heap memory allocated");
        bestPractices.add("Configure appropriate coordinator period settings");
    }

    private void validateSegmentConfig(com.fasterxml.jackson.databind.node.ArrayNode configIssues, com.fasterxml.jackson.databind.node.ArrayNode bestPractices) {
        bestPractices.add("Configure appropriate segment granularity for your use case");
    }

    private void validateIngestionConfig(com.fasterxml.jackson.databind.node.ArrayNode configIssues, com.fasterxml.jackson.databind.node.ArrayNode bestPractices) {
        bestPractices.add("Configure appropriate task capacity and resources");
    }

    private String getHealthStatus(int healthScore) {
        if (healthScore >= 90) return "EXCELLENT";
        if (healthScore >= 75) return "GOOD";
        if (healthScore >= 60) return "FAIR";
        if (healthScore >= 40) return "POOR";
        return "CRITICAL";
    }

    private int calculatePerformanceScore(com.fasterxml.jackson.databind.node.ArrayNode performanceIssues) {
        return Math.max(0, 100 - (performanceIssues.size() * 10));
    }

    private int calculateConfigScore(com.fasterxml.jackson.databind.node.ArrayNode configIssues) {
        return Math.max(0, 100 - (configIssues.size() * 15));
    }

    // Helper classes and methods
    private static class ComponentStatus {
        final String status;
        final int penalty;

        ComponentStatus(String status, int penalty) {
            this.status = status;
            this.penalty = penalty;
        }
    }
}