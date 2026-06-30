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

package com.iunera.druidmcpserver.ingestion.supervisors;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class SupervisorsTools {

    private final SupervisorsRepository supervisorsRepository;
    private final ObjectMapper objectMapper;

    public SupervisorsTools(SupervisorsRepository supervisorsRepository, ObjectMapper objectMapper) {
        this.supervisorsRepository = supervisorsRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get supervisors (list all or get status of specific)
     */
    @McpTool(
            description = "List all supervisors or query details of a specific supervisor. Parameters: [supervisorId] (String, optional) to get status details for a single supervisor.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, idempotentHint = true, destructiveHint = false)
    )
    public String getSupervisors(
            @McpToolParam(description = "ID of the supervisor (optional)", required = false) String supervisorId
    ) {
        try {
            if (supervisorId != null && !supervisorId.trim().isEmpty()) {
                JsonNode result = supervisorsRepository.getSupervisorStatus(supervisorId);
                return objectMapper.writeValueAsString(result);
            }
            JsonNode result = supervisorsRepository.getAllSupervisors();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting supervisors: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process supervisors request: %s", e.getMessage());
        }
    }

    /**
     * Manage supervisor state (suspend, resume, terminate)
     */
    @McpTool(
            description = "Suspend, resume, or terminate a supervisor's execution. Parameters: [supervisorId] (String, required), and [action] (Enum: SUSPEND, RESUME, TERMINATE, required).",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, idempotentHint = false, destructiveHint = true)
    )
    public String manageSupervisor(
            @McpToolParam(description = "ID of the supervisor (required)", required = true) String supervisorId,
            @McpToolParam(description = "Action to perform: SUSPEND, RESUME, TERMINATE (required)", required = true) String action
    ) {
        try {
            if (supervisorId == null || supervisorId.trim().isEmpty()) {
                return "Error: [supervisorId] parameter is required";
            }
            if (action == null || action.trim().isEmpty()) {
                return "Error: [action] parameter is required";
            }
            switch (action.toUpperCase()) {
                case "SUSPEND":
                    return objectMapper.writeValueAsString(supervisorsRepository.suspendSupervisor(supervisorId));
                case "RESUME":
                    return objectMapper.writeValueAsString(supervisorsRepository.resumeSupervisor(supervisorId));
                case "TERMINATE":
                    return objectMapper.writeValueAsString(supervisorsRepository.terminateSupervisor(supervisorId));
                default:
                    return String.format("Error: Unsupported action '%s'. Supported: SUSPEND, RESUME, TERMINATE", action);
            }
        } catch (RestClientException e) {
            return String.format("Error executing action '%s' on supervisor '%s': %s", action, supervisorId, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process action '%s' request on supervisor '%s': %s", action, supervisorId, e.getMessage());
        }
    }
}
