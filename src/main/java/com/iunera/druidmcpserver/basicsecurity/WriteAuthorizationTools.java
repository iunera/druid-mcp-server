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

@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
@ConditionalOnProperty(prefix = "druid.extension.druid-basic-security", name = "enabled", havingValue = "true", matchIfMissing = true)
@Component
public class WriteAuthorizationTools {

    private final SecurityRepository securityRepository;
    private final ObjectMapper objectMapper;

    public WriteAuthorizationTools(SecurityRepository securityRepository, ObjectMapper objectMapper) {
        this.securityRepository = securityRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new user in the authorization system
     */
    @McpTool(description = "Create a new user in the Druid authorization system")
    public String createAuthorizationUser(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Username to create", required = true)
            String userName) {
        try {
            JsonNode result = securityRepository.createAuthorizationUser(authorizerName, userName);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "User '" + userName + "' created successfully in authorizer '" + authorizerName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to create authorization user: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Delete a user from the authorization system
     */
    @McpTool(description = "Delete a user from the Druid authorization system. Use with caution as this action cannot be undone.")
    public String deleteAuthorizationUser(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Username to delete", required = true)
            String userName) {
        try {
            JsonNode result = securityRepository.deleteAuthorizationUser(authorizerName, userName);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "User '" + userName + "' deleted successfully from authorizer '" + authorizerName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to delete authorization user: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Create a new role
     */
    @McpTool(description = "Create a new role in the Druid authorization system")
    public String createRole(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Role name to create", required = true)
            String roleName) {
        try {
            JsonNode result = securityRepository.createRole(authorizerName, roleName);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "Role '" + roleName + "' created successfully in authorizer '" + authorizerName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to create role: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Delete a role
     */
    @McpTool(description = "Delete a role from the Druid authorization system. Use with caution as this action cannot be undone.")
    public String deleteRole(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Role name to delete", required = true)
            String roleName) {
        try {
            JsonNode result = securityRepository.deleteRole(authorizerName, roleName);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "Role '" + roleName + "' deleted successfully from authorizer '" + authorizerName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to delete role: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Set role permissions
     */
    @McpTool(description = "Set permissions for a role in the Druid authorization system. Provide permissions as JSON array.")
    public String setRolePermissions(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Role name to update", required = true)
            String roleName,
            @McpToolParam(description = "Permissions JSON array (e.g., '[{\"resource\":{\"name\":\".*\",\"type\":\"DATASOURCE\"},\"action\":\"READ\"}]')", required = true)
            String permissions) {
        try {
            JsonNode result = securityRepository.setRolePermissions(authorizerName, roleName, permissions);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "Permissions updated successfully for role '" + roleName + "' in authorizer '" + authorizerName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to set role permissions: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Assign role to user
     */
    @McpTool(description = "Assign a role to a user in the Druid authorization system")
    public String assignRoleToUser(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Username to assign role to", required = true)
            String userName,
            @McpToolParam(description = "Role name to assign", required = true)
            String roleName) {
        try {
            JsonNode result = securityRepository.assignRoleToUser(authorizerName, userName, roleName);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "Role '" + roleName + "' assigned successfully to user '" + userName + "' in authorizer '" + authorizerName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to assign role to user: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Unassign role from user
     */
    @McpTool(description = "Unassign a role from a user in the Druid authorization system")
    public String unassignRoleFromUser(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true)
            String authorizerName,
            @McpToolParam(description = "Username to unassign role from", required = true)
            String userName,
            @McpToolParam(description = "Role name to unassign", required = true)
            String roleName) {
        try {
            JsonNode result = securityRepository.unassignRoleFromUser(authorizerName, userName, roleName);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "Role '" + roleName + "' unassigned successfully from user '" + userName + "' in authorizer '" + authorizerName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to unassign role from user: " + e.getMessage())
                    .toString();
        }
    }
}
