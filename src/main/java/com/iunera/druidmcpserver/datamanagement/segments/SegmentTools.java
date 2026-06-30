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

package com.iunera.druidmcpserver.datamanagement.segments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class SegmentTools {

    private final SegmentRepository segmentRepository;
    private final ObjectMapper objectMapper;

    public SegmentTools(SegmentRepository segmentRepository,
                        ObjectMapper objectMapper) {
        this.segmentRepository = segmentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get segments (list all, filter by datasource/id, get metadata/details)
     */
    @McpTool(
            description = "Fetch segments for all or specific datasources, or get details for a single segment. Parameters: [datasource] (String, optional), [segmentId] (String, optional), [detailed] (Boolean, optional), [metadataOnly] (Boolean, optional) to restrict retrieval to core segment metadata.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, idempotentHint = true, destructiveHint = false)
    )
    public String getSegments(
            @McpToolParam(description = "Name of the datasource (optional)", required = false) String datasource,
            @McpToolParam(description = "ID of the specific segment (optional)", required = false) String segmentId,
            @McpToolParam(description = "Whether to include full segment details/metadata (optional)", required = false) Boolean detailed,
            @McpToolParam(description = "Whether to only fetch segment metadata from system tables (optional)", required = false) Boolean metadataOnly
    ) {
        try {
            if (segmentId != null && !segmentId.trim().isEmpty()) {
                if (datasource == null || datasource.trim().isEmpty()) {
                    return "Error: [datasource] parameter is required when [segmentId] is specified";
                }
                JsonNode result = segmentRepository.getSegmentDetails(datasource, segmentId);
                return objectMapper.writeValueAsString(result);
            }

            if (metadataOnly != null && metadataOnly) {
                if (datasource != null && !datasource.trim().isEmpty()) {
                    JsonNode result = segmentRepository.getSegmentMetadataForDatasource(datasource);
                    return objectMapper.writeValueAsString(result);
                } else {
                    JsonNode result = segmentRepository.getSegmentMetadata();
                    return objectMapper.writeValueAsString(result);
                }
            }

            if (datasource != null && !datasource.trim().isEmpty()) {
                if (detailed != null && detailed) {
                    JsonNode result = segmentRepository.getSegmentsForDatasourceWithDetails(datasource);
                    return objectMapper.writeValueAsString(result);
                } else {
                    JsonNode result = segmentRepository.getSegmentsForDatasource(datasource);
                    return objectMapper.writeValueAsString(result);
                }
            }

            JsonNode result = segmentRepository.getAllSegments();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting segments: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process segments request: %s", e.getMessage());
        }
    }

    /**
     * Get segment load queue status
     */
    @McpTool(
            description = "Get the load queue status showing segments currently being loaded. Parameters: [serverName] (String, optional) to filter by a specific server.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, idempotentHint = true, destructiveHint = false)
    )
    public String getSegmentLoadQueue(
            @McpToolParam(description = "Name of the server to get load queue for (optional)", required = false) String serverName
    ) {
        try {
            if (serverName != null && !serverName.trim().isEmpty()) {
                JsonNode result = segmentRepository.getLoadQueueStatusForServer(serverName);
                return objectMapper.writeValueAsString(result);
            }
            JsonNode result = segmentRepository.getLoadQueueStatus();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting load queue status: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process load queue status request: %s", e.getMessage());
        }
    }
}
