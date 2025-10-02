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
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@ConditionalOnProperty(prefix = "druid.extension.druid-basic-security", name = "enabled", havingValue = "true", matchIfMissing = true)
@Component
public class ReadAuthorizationTools {

    private final SecurityRepository securityRepository;
    private final ObjectMapper objectMapper;

    public ReadAuthorizationTools(SecurityRepository securityRepository, ObjectMapper objectMapper) {
        this.securityRepository = securityRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * List all users in the authorization system
     */
    @McpTool(description = "List all users in the Druid authorization system for a specific authorizer")
    public String listAuthorizationUsers(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName) {
        try {
            JsonNode result = securityRepository.getAllAuthorizationUsers(authorizerName);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to list authorization users: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get specific user details from the authorization system
     */
    @McpTool(description = "Get details of a specific user from the Druid authorization system including their roles")
    public String getAuthorizationUser(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Username to retrieve", required = true)
            String userName) {
        try {
            JsonNode result = securityRepository.getAuthorizationUser(authorizerName, userName);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get authorization user: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * List all roles in the authorization system
     */
    @McpTool(description = "List all roles in the Druid authorization system for a specific authorizer")
    public String listRoles(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName) {
        try {
            JsonNode result = securityRepository.getAllRoles(authorizerName);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to list roles: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get specific role details
     */
    @McpTool(description = "Get details of a specific role from the Druid authorization system including its permissions")
    public String getRole(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Role name to retrieve", required = true)
            String roleName) {
        try {
            JsonNode result = securityRepository.getRole(authorizerName, roleName);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get role: " + e.getMessage())
                    .toString();
        }
    }
}
