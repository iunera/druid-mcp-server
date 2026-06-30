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

package com.iunera.druidmcpserver.datamanagement.datasource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import com.iunera.druidmcpserver.datamanagement.segments.SegmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DatasourceTools {

    private final DatasourceRepository datasourceRepository;
    private final SegmentRepository segmentRepository;
    private final ObjectMapper objectMapper;

    public DatasourceTools(DatasourceRepository datasourceRepository,
                           SegmentRepository segmentRepository,
                           ObjectMapper objectMapper) {
        this.datasourceRepository = datasourceRepository;
        this.segmentRepository = segmentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get datasources (list all or show details for specific)
     */
    @McpTool(
            description = "List all available Apache Druid datasources or get detailed information for a specific datasource. Parameters: [datasourceName] (String, optional) to fetch details for a single datasource, and [detailed] (Boolean, optional) to include schema and column specifications.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, idempotentHint = true, destructiveHint = false)
    )
    public String getDatasources(
            @McpToolParam(description = "Name of the datasource to get details for (optional)", required = false) String datasourceName,
            @McpToolParam(description = "Whether to include detailed columns and data types (optional)", required = false) Boolean detailed
    ) {
        try {
            JsonNode result = datasourceRepository.getAllDatasources();

            if (datasourceName == null || datasourceName.trim().isEmpty()) {
                if (detailed != null && detailed) {
                    List<Map<String, Object>> detailedList = new ArrayList<>();
                    for (int i = 0; i < result.size(); i++) {
                        JsonNode datasource = result.get(i);
                        String currentName = datasource.has("TABLE_NAME") ?
                                datasource.get("TABLE_NAME").asText() : "datasource_" + i;
                        detailedList.add(datasourceRepository.buildDatasourceInfo(datasource, currentName));
                    }
                    return objectMapper.writeValueAsString(detailedList);
                } else {
                    List<String> datasourceNames = new ArrayList<>();
                    for (int i = 0; i < result.size(); i++) {
                        JsonNode datasource = result.get(i);
                        String name = datasource.has("TABLE_NAME") ?
                                datasource.get("TABLE_NAME").asText() : "datasource_" + i;
                        datasourceNames.add(name);
                    }
                    return objectMapper.writeValueAsString(datasourceNames);
                }
            }

            // Find specific datasource details
            for (int i = 0; i < result.size(); i++) {
                JsonNode datasource = result.get(i);
                String currentName = datasource.has("TABLE_NAME") ?
                        datasource.get("TABLE_NAME").asText() : "datasource_" + i;

                if (currentName.equals(datasourceName)) {
                    Map<String, Object> datasourceInfo = datasourceRepository.buildDatasourceInfo(datasource, datasourceName);
                    return objectMapper.writeValueAsString(datasourceInfo);
                }
            }

            return String.format("Datasource '%s' not found", datasourceName);
        } catch (RestClientException e) {
            return String.format("Error getting datasources: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process datasources request: %s", e.getMessage());
        }
    }

    /**
     * Manage a datasource or its segments
     */
    @McpTool(
            description = "Modify segment states or permanently drop a datasource. Parameters: [action] (Enum: ENABLE_SEGMENT, DISABLE_SEGMENT, KILL_DATASOURCE, required), [datasource] (String, required), [segmentId] (String, optional) to target a specific segment, and [interval] (String, optional) to specify the time range for killing data.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, idempotentHint = false, destructiveHint = true)
    )
    public String manageDatasourceOrSegment(
            @McpToolParam(description = "Action to perform: ENABLE_SEGMENT, DISABLE_SEGMENT, KILL_DATASOURCE (required)", required = true) String action,
            @McpToolParam(description = "Name of the datasource (required)", required = true) String datasource,
            @McpToolParam(description = "Name/ID of the segment (required only for ENABLE_SEGMENT/DISABLE_SEGMENT)", required = false) String segmentId,
            @McpToolParam(description = "Time interval for KILL_DATASOURCE, e.g. 1000-01-01/2025-07-06 (required only for KILL_DATASOURCE)", required = false) String interval
    ) {
        try {
            if (action == null) {
                return "Error: [action] parameter is required";
            }
            if (datasource == null) {
                return "Error: [datasource] parameter is required";
            }

            switch (action.toUpperCase()) {
                case "ENABLE_SEGMENT":
                    if (segmentId == null || segmentId.trim().isEmpty()) {
                        return "Error: [segmentId] is required for ENABLE_SEGMENT action";
                    }
                    JsonNode enableResult = segmentRepository.enableSegment(datasource, segmentId);
                    return objectMapper.writeValueAsString(enableResult);

                case "DISABLE_SEGMENT":
                    if (segmentId == null || segmentId.trim().isEmpty()) {
                        return "Error: [segmentId] is required for DISABLE_SEGMENT action";
                    }
                    JsonNode disableResult = segmentRepository.disableSegment(datasource, segmentId);
                    return objectMapper.writeValueAsString(disableResult);

                case "KILL_DATASOURCE":
                    if (interval == null || interval.trim().isEmpty()) {
                        return "Error: [interval] is required for KILL_DATASOURCE action";
                    }
                    JsonNode killResult = datasourceRepository.killDatasource(datasource, interval);
                    if (killResult == null) {
                        return String.format("Kill task for datasource '%s' was submitted successfully (no response body)", datasource);
                    }
                    return objectMapper.writeValueAsString(killResult);

                default:
                    return String.format("Error: Unsupported action '%s'. Supported: ENABLE_SEGMENT, DISABLE_SEGMENT, KILL_DATASOURCE", action);
            }
        } catch (RestClientException e) {
            return String.format("Error executing action '%s' on datasource '%s': %s", action, datasource, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process action '%s' request on datasource '%s': %s", action, datasource, e.getMessage());
        }
    }
}
