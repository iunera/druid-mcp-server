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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.iunera.druidmcpserver.datamanagement.datasource.DatasourceRepository;
import com.iunera.druidmcpserver.datamanagement.segments.SegmentRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.ClusterRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import com.iunera.druidmcpserver.monitoring.health.repository.ServerRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
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
     * Get cluster status metrics or metadata
     */
    @McpTool(
            description = "Check overall health or fetch specific metadata/properties from coordinators or routers. Parameters: [aspect] (Enum: OVERALL, COORDINATOR, ROUTER, LEADER, METADATA, PROPERTIES, SELF_DISCOVERY_COORDINATOR, SELF_DISCOVERY_ROUTER, optional).",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, idempotentHint = true, destructiveHint = false)
    )
    public String getClusterStatus(
            @McpToolParam(description = "Aspect to retrieve: OVERALL, COORDINATOR, ROUTER, LEADER, METADATA, PROPERTIES, SELF_DISCOVERY_COORDINATOR, SELF_DISCOVERY_ROUTER (optional, defaults to OVERALL)", required = false) String aspect
    ) {
        String queryAspect = aspect == null ? "OVERALL" : aspect.toUpperCase();
        try {
            switch (queryAspect) {
                case "COORDINATOR":
                    return healthStatusRepository.getCoordinatorHealth().toString();
                case "ROUTER":
                    return healthStatusRepository.getRouterHealth().toString();
                case "SELF_DISCOVERY_COORDINATOR":
                    return healthStatusRepository.getCoordinatorSelfDiscovered().toString();
                case "SELF_DISCOVERY_ROUTER":
                    return healthStatusRepository.getRouterSelfDiscovered().toString();
                case "LEADER":
                    return clusterRepository.getLeaderInfo().toString();
                case "METADATA":
                    return clusterRepository.getClusterMetadata().toString();
                case "PROPERTIES":
                    var propertiesNode = objectMapper.createObjectNode();
                    try {
                        propertiesNode.set("coordinator", healthStatusRepository.getCoordinatorProperties());
                    } catch (Exception e) {
                        propertiesNode.put("coordinator_error", e.getMessage());
                    }
                    try {
                        propertiesNode.set("router", healthStatusRepository.getRouterProperties());
                    } catch (Exception e) {
                        propertiesNode.put("router_error", e.getMessage());
                    }
                    return propertiesNode.toString();
                case "OVERALL":
                default:
                    var healthReport = objectMapper.createObjectNode();
                    try {
                        healthReport.set("coordinator_health", healthStatusRepository.getCoordinatorHealth());
                    } catch (RestClientException e) {
                        healthReport.put("coordinator_health_error", e.getMessage());
                    }
                    try {
                        healthReport.set("router_health", healthStatusRepository.getRouterHealth());
                    } catch (RestClientException e) {
                        healthReport.put("router_health_error", e.getMessage());
                    }
                    try {
                        healthReport.set("leader_info", clusterRepository.getLeaderInfo());
                    } catch (RestClientException e) {
                        healthReport.put("leader_info_error", e.getMessage());
                    }
                    try {
                        JsonNode datasources = datasourceRepository.getAllDatasources();
                        if (datasources.isArray()) {
                            healthReport.put("datasource_count", datasources.size());
                        }
                    } catch (RestClientException e) {
                        healthReport.put("datasource_count_error", e.getMessage());
                    }
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
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", String.format("Failed to get cluster status for aspect %s: %s", queryAspect, e.getMessage()))
                    .toString();
        }
    }

    /**
     * Get status of nodes / servers
     */
    @McpTool(
            description = "List registered servers, their detailed status, or single node status. Parameters: [serverName] (String, optional), and [detailed] (Boolean, optional) to include complete node metadata.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, idempotentHint = true, destructiveHint = false)
    )
    public String getNodesStatus(
            @McpToolParam(description = "Name of the server to filter by (optional, use 'broker' to get broker status)", required = false) String serverName,
            @McpToolParam(description = "Whether to retrieve full node metadata details (optional)", required = false) Boolean detailed
    ) {
        try {
            if (serverName != null && !serverName.trim().isEmpty()) {
                if ("broker".equalsIgnoreCase(serverName)) {
                    return serverRepository.getBrokerStatus().toString();
                }
                return serverRepository.getServerStatus(serverName).toString();
            }

            if (detailed != null && detailed) {
                return serverRepository.getAllServersStatusWithDetails().toString();
            }

            return serverRepository.getAllServersStatus().toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", String.format("Failed to get nodes status: %s", e.getMessage()))
                    .toString();
        }
    }
}