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

package com.iunera.druidmcpserver.basicsecurity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class ReadAuthenticationTools {

    private final SecurityRepository securityRepository;
    private final HealthStatusRepository healthStatusRepository;
    private final ObjectMapper objectMapper;

    public ReadAuthenticationTools(SecurityRepository securityRepository, HealthStatusRepository healthStatusRepository, ObjectMapper objectMapper) {
        this.securityRepository = securityRepository;
        this.healthStatusRepository = healthStatusRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * List all users in the authentication system
     */
    @McpTool(description = "List all users in the Druid authentication system for a specific authenticator")
    public String listAuthenticationUsers(
            @McpToolParam(description = "Name of the authenticator (e.g., 'db')", required = true)
            String authenticatorName) {
        try {
            JsonNode result = securityRepository.getAllUsers(authenticatorName);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to list authentication users: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get specific user details from the authentication system
     */
    @McpTool(description = "Get details of a specific user from the Druid authentication system")
    public String getAuthenticationUser(
            @McpToolParam(description = "Name of the authenticator (e.g., 'db')", required = true)
            String authenticatorName,
            @McpToolParam(description = "Username to retrieve", required = true)
            String userName) {
        try {
            JsonNode result = securityRepository.getUser(authenticatorName, userName);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get authentication user: " + e.getMessage())
                    .toString();
        }
    }
}
