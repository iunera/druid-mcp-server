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
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
@Component
public class WriteSegmentTools {

    private final SegmentRepository segmentRepository;
    private final ObjectMapper objectMapper;

    public WriteSegmentTools(SegmentRepository segmentRepository,
                             ObjectMapper objectMapper) {
        this.segmentRepository = segmentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Enable a segment
     */
    @McpTool(description = "Enable a specific segment by datasource and segment ID")
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
    @McpTool(description = "Disable a specific segment by datasource and segment ID")
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
}