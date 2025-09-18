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

package com.iunera.druidmcpserver.monitoring.health.basic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iunera.druidmcpserver.datamanagement.datasource.DatasourceRepository;
import com.iunera.druidmcpserver.datamanagement.segments.SegmentRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.ClusterRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.ServerRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Basic Health Tool Provider for Druid MCP Server
 * Provides fundamental health check tools for monitoring Druid cluster status
 */
@Component
public class HealthToolProvider {

    private final HealthStatusRepository healthStatusRepository;
    private final ServerRepository serverRepository;
    private final ClusterRepository clusterRepository;
    private final SegmentRepository segmentRepository;
    private final DatasourceRepository datasourceRepository;
    private final ObjectMapper objectMapper;

    public HealthToolProvider(HealthStatusRepository healthStatusRepository,
                              ServerRepository serverRepository,
                              ClusterRepository clusterRepository,
                              SegmentRepository segmentRepository,
                              DatasourceRepository datasourceRepository,
                              ObjectMapper objectMapper) {
        this.healthStatusRepository = healthStatusRepository;
        this.serverRepository = serverRepository;
        this.clusterRepository = clusterRepository;
        this.segmentRepository = segmentRepository;
        this.datasourceRepository = datasourceRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Check overall Druid cluster health
     */
    @McpTool(description = "Check overall Druid cluster health including coordinator and router status")
    public String checkClusterHealth() {
        var healthReport = objectMapper.createObjectNode();

        // Check coordinator health
        try {
            JsonNode coordinatorHealth = healthStatusRepository.getCoordinatorHealth();
            healthReport.set("coordinator_health", coordinatorHealth);
        } catch (RestClientException e) {
            healthReport.put("coordinator_health_error", e.getMessage());
        }

        // Check router health
        try {
            JsonNode routerHealth = healthStatusRepository.getRouterHealth();
            healthReport.set("router_health", routerHealth);
        } catch (RestClientException e) {
            healthReport.put("router_health_error", e.getMessage());
        }

        // Check leader info
        try {
            JsonNode leaderInfo = clusterRepository.getLeaderInfo();
            healthReport.set("leader_info", leaderInfo);
        } catch (RestClientException e) {
            healthReport.put("leader_info_error", e.getMessage());
        }

        // Check datasource count
        try {
            JsonNode datasources = datasourceRepository.getAllDatasources();
            if (datasources.isArray()) {
                healthReport.put("datasource_count", datasources.size());
            }
        } catch (RestClientException e) {
            healthReport.put("datasource_count_error", e.getMessage());
        }

        // Check segment count
        try {
            JsonNode segments = segmentRepository.getAllSegments();
            if (segments.isArray()) {
                healthReport.put("segment_count", segments.size());
            }
        } catch (RestClientException e) {
            healthReport.put("segment_count_error", e.getMessage());
        }

        return healthReport.toString();
    }

    /**
     * Get coordinator health status
     */
    @McpTool(description = "Get Druid coordinator health status")
    public String getCoordinatorHealth() {
        try {
            JsonNode result = healthStatusRepository.getCoordinatorHealth();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get coordinator health: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get router health status
     */
    @McpTool(description = "Get Druid router health status")
    public String getRouterHealth() {
        try {
            JsonNode result = healthStatusRepository.getRouterHealth();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get router health: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get coordinator self-discovery status
     */
    @McpTool(description = "Get Druid coordinator self-discovery status")
    public String getCoordinatorSelfDiscovered() {
        try {
            JsonNode result = healthStatusRepository.getCoordinatorSelfDiscovered();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get coordinator self-discovery status: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get router self-discovery status
     */
    @McpTool(description = "Get Druid router self-discovery status")
    public String getRouterSelfDiscovered() {
        try {
            JsonNode result = healthStatusRepository.getRouterSelfDiscovered();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get router self-discovery status: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get all servers status
     */
    @McpTool(description = "Get status of all Druid servers")
    public String getAllServersStatus() {
        try {
            JsonNode result = serverRepository.getAllServersStatus();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get servers status: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get all servers status with details
     */
    @McpTool(description = "Get detailed status of all Druid servers")
    public String getAllServersStatusWithDetails() {
        try {
            JsonNode result = serverRepository.getAllServersStatusWithDetails();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get detailed servers status: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get specific server status
     */
    @McpTool(description = "Get status of a specific Druid server")
    public String getServerStatus(String serverName) {
        try {
            JsonNode result = serverRepository.getServerStatus(serverName);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get server status for " + serverName + ": " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get cluster metadata
     */
    @McpTool(description = "Get Druid cluster configuration metadata")
    public String getClusterMetadata() {
        try {
            JsonNode result = clusterRepository.getClusterMetadata();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get cluster metadata: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get leader information
     */
    @McpTool(description = "Get Druid cluster leader information")
    public String getLeaderInfo() {
        try {
            JsonNode result = clusterRepository.getLeaderInfo();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get leader info: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Check if coordinator is leader
     */
    @McpTool(description = "Check if coordinator is the cluster leader")
    public String isCoordinatorLeader() {
        try {
            JsonNode result = clusterRepository.isCoordinatorLeader();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to check coordinator leader status: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get coordinator properties
     */
    @McpTool(description = "Get Druid coordinator properties")
    public String getCoordinatorProperties() {
        try {
            JsonNode result = healthStatusRepository.getCoordinatorProperties();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get coordinator properties: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get router properties
     */
    @McpTool(description = "Get Druid router properties")
    public String getRouterProperties() {
        try {
            JsonNode result = healthStatusRepository.getRouterProperties();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get router properties: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get broker status
     */
    @McpTool(description = "Get Druid broker status through router")
    public String getBrokerStatus() {
        try {
            JsonNode result = serverRepository.getBrokerStatus();
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get broker status: " + e.getMessage())
                    .toString();
        }
    }
}