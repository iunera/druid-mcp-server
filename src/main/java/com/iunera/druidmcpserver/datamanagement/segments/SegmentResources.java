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
import org.springaicommunity.mcp.annotation.McpResource;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SegmentResources {

    private final SegmentRepository segmentRepository;
    private final ObjectMapper objectMapper;

    public SegmentResources(SegmentRepository segmentRepository,
                            ObjectMapper objectMapper) {
        this.segmentRepository = segmentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get basic information for a specific segment
     */
    @McpResource(uri = "segment://{segmentid}", name = "Segment", description = "Provides basic information for a specific Druid segment")
    public ReadResourceResult getSegment(ReadResourceRequest request, String segmentid) {
        try {
            JsonNode allSegments = segmentRepository.getSegmentMetadata();

            // Find the specific segment
            JsonNode targetSegment = null;
            if (allSegments.isArray()) {
                for (int i = 0; i < allSegments.size(); i++) {
                    JsonNode segment = allSegments.get(i);
                    String currentSegmentId = segment.has("segment_id") ?
                            segment.get("segment_id").asText() : "segment_" + i;

                    if (currentSegmentId.equals(segmentid)) {
                        targetSegment = segment;
                        break;
                    }
                }
            }

            if (targetSegment == null) {
                String errorMessage = String.format("Segment '%s' not found", segmentid);
                return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
            }

            String segmentJson = objectMapper.writeValueAsString(objectMapper.convertValue(targetSegment, Map.class));
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "application/json", segmentJson)));

        } catch (RestClientException e) {
            String errorMessage = String.format("Error retrieving segment '%s': %s", segmentid, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to process segment '%s': %s", segmentid, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
        }
    }

    /**
     * Get detailed information for a specific segment including metadata details
     */
    @McpResource(uri = "segment-details://{segmentid}", name = "Segment Details", description = "Provides detailed information for a specific Druid segment including metadata details")
    public ReadResourceResult getSegmentDetails(String segmentid) {
        try {
            JsonNode allSegments = segmentRepository.getSegmentMetadata();

            // Find the specific segment
            JsonNode targetSegment = null;
            String datasourceName = "unknown_datasource";
            if (allSegments.isArray()) {
                for (int i = 0; i < allSegments.size(); i++) {
                    JsonNode segment = allSegments.get(i);
                    String currentSegmentId = segment.has("segment_id") ?
                            segment.get("segment_id").asText() : "segment_" + i;

                    if (currentSegmentId.equals(segmentid)) {
                        targetSegment = segment;
                        datasourceName = segment.has("datasource") ?
                                segment.get("datasource").asText() : "unknown_datasource";
                        break;
                    }
                }
            }

            if (targetSegment == null) {
                String errorMessage = String.format("Segment '%s' not found", segmentid);
                return new ReadResourceResult(List.of(new TextResourceContents("segment-details://" + segmentid, "text/plain", errorMessage)));
            }

            // Get detailed information for this segment
            Map<String, Object> segmentInfo = buildSegmentInfo(targetSegment, datasourceName, segmentid);
            String segmentJson = objectMapper.writeValueAsString(segmentInfo);

            return new ReadResourceResult(List.of(new TextResourceContents("segment-details://" + segmentid, "application/json", segmentJson)));

        } catch (RestClientException e) {
            String errorMessage = String.format("Error retrieving segment details for '%s': %s", segmentid, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents("segment-details://" + segmentid, "text/plain", errorMessage)));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to process segment details for '%s': %s", segmentid, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents("segment-details://" + segmentid, "text/plain", errorMessage)));
        }
    }

    /**
     * Build segment information from the segment data
     */
    private Map<String, Object> buildSegmentInfo(JsonNode segment, String datasourceName, String segmentId) {
        Map<String, Object> segmentInfo = new HashMap<>();

        segmentInfo.put("datasource", datasourceName);
        segmentInfo.put("segment_id", segmentId);

        // Add common segment fields
        if (segment.has("start")) {
            segmentInfo.put("start", segment.get("start").asText());
        }
        if (segment.has("end")) {
            segmentInfo.put("end", segment.get("end").asText());
        }
        if (segment.has("version")) {
            segmentInfo.put("version", segment.get("version").asText());
        }
        if (segment.has("partition_num")) {
            segmentInfo.put("partition_num", segment.get("partition_num").asInt());
        }
        if (segment.has("num_replicas")) {
            segmentInfo.put("num_replicas", segment.get("num_replicas").asInt());
        }
        if (segment.has("num_rows")) {
            segmentInfo.put("num_rows", segment.get("num_rows").asLong());
        }
        if (segment.has("size")) {
            segmentInfo.put("size", segment.get("size").asLong());
        }
        if (segment.has("is_published")) {
            segmentInfo.put("is_published", segment.get("is_published").asBoolean());
        }
        if (segment.has("is_available")) {
            segmentInfo.put("is_available", segment.get("is_available").asBoolean());
        }
        if (segment.has("is_realtime")) {
            segmentInfo.put("is_realtime", segment.get("is_realtime").asBoolean());
        }
        if (segment.has("is_overshadowed")) {
            segmentInfo.put("is_overshadowed", segment.get("is_overshadowed").asBoolean());
        }

        // Add the full segment data for reference
        segmentInfo.put("full_segment", segment);

        return segmentInfo;
    }
}
