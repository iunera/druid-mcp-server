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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class ReadSupervisorsTools {

    private final SupervisorsRepository supervisorsRepository;
    private final ObjectMapper objectMapper;

    public ReadSupervisorsTools(SupervisorsRepository supervisorsRepository, ObjectMapper objectMapper) {
        this.supervisorsRepository = supervisorsRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * List all supervisors
     */
    @McpTool(description = "List all Druid supervisors")
    public String listSupervisors() {
        try {
            JsonNode result = supervisorsRepository.getAllSupervisors();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error listing supervisors: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to list supervisors: %s", e.getMessage());
        }
    }

    /**
     * Get supervisor status
     */
    @McpTool(description = "Get the status of a Druid supervisor by supervisor ID")
    public String getSupervisorStatus(String supervisorId) {
        try {
            JsonNode result = supervisorsRepository.getSupervisorStatus(supervisorId);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error getting supervisor status: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to get supervisor status: %s", e.getMessage());
        }
    }
}