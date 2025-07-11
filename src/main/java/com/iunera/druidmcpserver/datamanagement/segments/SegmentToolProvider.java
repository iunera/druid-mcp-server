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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class SegmentToolProvider {

    private final SegmentRepository segmentRepository;
    private final ObjectMapper objectMapper;

    public SegmentToolProvider(SegmentRepository segmentRepository,
                               ObjectMapper objectMapper) {
        this.segmentRepository = segmentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * List all segments for all datasources
     */
    @Tool(description = "List all segments for all datasources with full details")
    public String listAllSegments() {
        try {
            JsonNode result = segmentRepository.getAllSegments();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error listing all segments: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process segments response: %s", e.getMessage());
        }
    }

    /**
     * Get segments for a specific datasource
     */
    @Tool(description = "Get segments for a specific datasource")
    public String getSegmentsForDatasource(String datasourceName) {
        try {
            JsonNode result = segmentRepository.getSegmentsForDatasource(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting segments for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process segments response for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * Get segments for a specific datasource with full details
     */
    @Tool(description = "Get segments for a specific datasource with full details including metadata")
    public String getSegmentsWithDetailsForDatasource(String datasourceName) {
        try {
            JsonNode result = segmentRepository.getSegmentsForDatasourceWithDetails(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting detailed segments for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process detailed segments response for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * Get specific segment details
     */
    @Tool(description = "Get details for a specific segment by datasource and segment ID")
    public String getSegmentDetails(String datasourceName, String segmentId) {
        try {
            JsonNode result = segmentRepository.getSegmentDetails(datasourceName, segmentId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting segment details for '%s' in datasource '%s': %s", segmentId, datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process segment details for '%s' in datasource '%s': %s", segmentId, datasourceName, e.getMessage());
        }
    }

    /**
     * Enable a segment
     */
    @Tool(description = "Enable a specific segment by datasource and segment ID")
    public String enableSegment(String datasourceName, String segmentId) {
        try {
            JsonNode result = segmentRepository.enableSegment(datasourceName, segmentId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error enabling segment '%s' in datasource '%s': %s", segmentId, datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to enable segment '%s' in datasource '%s': %s", segmentId, datasourceName, e.getMessage());
        }
    }

    /**
     * Disable a segment
     */
    @Tool(description = "Disable a specific segment by datasource and segment ID")
    public String disableSegment(String datasourceName, String segmentId) {
        try {
            JsonNode result = segmentRepository.disableSegment(datasourceName, segmentId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error disabling segment '%s' in datasource '%s': %s", segmentId, datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to disable segment '%s' in datasource '%s': %s", segmentId, datasourceName, e.getMessage());
        }
    }

    /**
     * Get segment metadata using SQL query
     */
    @Tool(description = "Get segment metadata for all datasources using SQL query from sys.segments table")
    public String getSegmentMetadata() {
        try {
            JsonNode result = segmentRepository.getSegmentMetadata();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting segment metadata: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process segment metadata response: %s", e.getMessage());
        }
    }

    /**
     * Get segment metadata for a specific datasource using SQL
     */
    @Tool(description = "Get segment metadata for a specific datasource using SQL query from sys.segments table")
    public String getSegmentMetadataForDatasource(String datasourceName) {
        try {
            JsonNode result = segmentRepository.getSegmentMetadataForDatasource(datasourceName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting segment metadata for datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process segment metadata for datasource '%s': %s", datasourceName, e.getMessage());
        }
    }

    /**
     * Get load queue status
     */
    @Tool(description = "Get load queue status for all servers showing segments being loaded")
    public String getLoadQueueStatus() {
        try {
            JsonNode result = segmentRepository.getLoadQueueStatus();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting load queue status: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process load queue status response: %s", e.getMessage());
        }
    }

    /**
     * Get load queue status for a specific server
     */
    @Tool(description = "Get load queue status for a specific server showing segments being loaded on that server")
    public String getLoadQueueStatusForServer(String serverName) {
        try {
            JsonNode result = segmentRepository.getLoadQueueStatusForServer(serverName);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting load queue status for server '%s': %s", serverName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process load queue status for server '%s': %s", serverName, e.getMessage());
        }
    }
}