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

package com.iunera.druidmcpserver.druidbasicsecurityextension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Profile("permissions")
@ConditionalOnExpression("!'${druid.coordinator.url:}'.isEmpty()")
@Component
public class DruidBasicSecurityExtensionTools {

    private final DruidBasicSecurityExtensionRepository druidBasicSecurityExtensionRepository;
    private final ObjectMapper objectMapper;

    public DruidBasicSecurityExtensionTools(DruidBasicSecurityExtensionRepository druidBasicSecurityExtensionRepository, ObjectMapper objectMapper) {
        this.druidBasicSecurityExtensionRepository = druidBasicSecurityExtensionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Administer basic security users and credentials.
     */
    @McpTool(
            description = "Administer basic security users and passwords. Parameters: [authenticator] (String, required), [action] (Enum: LIST, GET, CREATE, DELETE, SET_PASSWORD, required), [username] (String, optional), and [password] (String, optional) to set user credentials.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, idempotentHint = false, destructiveHint = true)
    )
    public String manageAuthentication(
            @McpToolParam(description = "Name of the authenticator (e.g., 'db')", required = true) String authenticator,
            @McpToolParam(description = "Action to perform: LIST, GET, CREATE, DELETE, SET_PASSWORD", required = true) String action,
            @McpToolParam(description = "Username (optional/required depending on action)", required = false) String username,
            @McpToolParam(description = "Password for the user (required only for SET_PASSWORD)", required = false) String password
    ) {
        if (action == null) {
            return "Error: [action] parameter is required. Allowed values: LIST, GET, CREATE, DELETE, SET_PASSWORD";
        }
        String act = action.toUpperCase();
        try {
            switch (act) {
                case "LIST":
                    return druidBasicSecurityExtensionRepository.getAllUsers(authenticator).toString();
                case "GET":
                    if (username == null) {
                        return "Error: [username] is required for GET action.";
                    }
                    return druidBasicSecurityExtensionRepository.getUser(authenticator, username).toString();
                case "CREATE":
                    if (username == null) {
                        return "Error: [username] is required for CREATE action.";
                    }
                    druidBasicSecurityExtensionRepository.createUser(authenticator, username);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "User '" + username + "' created successfully in authenticator '" + authenticator + "'")
                            .toString();
                case "DELETE":
                    if (username == null) {
                        return "Error: [username] is required for DELETE action.";
                    }
                    druidBasicSecurityExtensionRepository.deleteUser(authenticator, username);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "User '" + username + "' deleted successfully from authenticator '" + authenticator + "'")
                            .toString();
                case "SET_PASSWORD":
                    if (username == null || password == null) {
                        return "Error: [username] and [password] are required for SET_PASSWORD action.";
                    }
                    druidBasicSecurityExtensionRepository.setUserCredentials(authenticator, username, password);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "Password updated successfully for user '" + username + "' in authenticator '" + authenticator + "'")
                            .toString();
                default:
                    return "Error: Invalid action: " + action + ". Allowed values: LIST, GET, CREATE, DELETE, SET_PASSWORD";
            }
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to manage authentication: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Manage security authorization properties, roles, and resource access policies.
     */
    @McpTool(
            description = "Manage security authorization properties, roles, and resource access policies. Parameters: [authorizer] (String, required), [action] (Enum: LIST_USERS, GET_USER, CREATE_USER, DELETE_USER, LIST_ROLES, GET_ROLE, CREATE_ROLE, DELETE_ROLE, SET_PERMISSIONS, required), [name] (String, optional) specifying user name or role name, and [permissionsJson] (String, optional) specifying a JSON permissions list.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, idempotentHint = false, destructiveHint = true)
    )
    public String manageAuthorization(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true) String authorizer,
            @McpToolParam(description = "Action to perform: LIST_USERS, GET_USER, CREATE_USER, DELETE_USER, LIST_ROLES, GET_ROLE, CREATE_ROLE, DELETE_ROLE, SET_PERMISSIONS", required = true) String action,
            @McpToolParam(description = "User name or role name (optional/required depending on action)", required = false) String name,
            @McpToolParam(description = "Permissions JSON array string (required only for SET_PERMISSIONS)", required = false) String permissionsJson
    ) {
        if (action == null) {
            return "Error: [action] parameter is required.";
        }
        String act = action.toUpperCase();
        try {
            switch (act) {
                case "LIST_USERS":
                    return druidBasicSecurityExtensionRepository.getAllAuthorizationUsers(authorizer).toString();
                case "GET_USER":
                    if (name == null) return "Error: [name] (username) is required for GET_USER.";
                    return druidBasicSecurityExtensionRepository.getAuthorizationUser(authorizer, name).toString();
                case "CREATE_USER":
                    if (name == null) return "Error: [name] (username) is required for CREATE_USER.";
                    druidBasicSecurityExtensionRepository.createAuthorizationUser(authorizer, name);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "Authorization user '" + name + "' created successfully in authorizer '" + authorizer + "'")
                            .toString();
                case "DELETE_USER":
                    if (name == null) return "Error: [name] (username) is required for DELETE_USER.";
                    druidBasicSecurityExtensionRepository.deleteAuthorizationUser(authorizer, name);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "Authorization user '" + name + "' deleted successfully from authorizer '" + authorizer + "'")
                            .toString();
                case "LIST_ROLES":
                    return druidBasicSecurityExtensionRepository.getAllRoles(authorizer).toString();
                case "GET_ROLE":
                    if (name == null) return "Error: [name] (role name) is required for GET_ROLE.";
                    return druidBasicSecurityExtensionRepository.getRole(authorizer, name).toString();
                case "CREATE_ROLE":
                    if (name == null) return "Error: [name] (role name) is required for CREATE_ROLE.";
                    druidBasicSecurityExtensionRepository.createRole(authorizer, name);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "Role '" + name + "' created successfully in authorizer '" + authorizer + "'")
                            .toString();
                case "DELETE_ROLE":
                    if (name == null) return "Error: [name] (role name) is required for DELETE_ROLE.";
                    druidBasicSecurityExtensionRepository.deleteRole(authorizer, name);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "Role '" + name + "' deleted successfully from authorizer '" + authorizer + "'")
                            .toString();
                case "SET_PERMISSIONS":
                    if (name == null || permissionsJson == null) {
                        return "Error: [name] (role name) and [permissionsJson] are required for SET_PERMISSIONS.";
                    }
                    druidBasicSecurityExtensionRepository.setRolePermissions(authorizer, name, permissionsJson);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "Permissions updated successfully for role '" + name + "' in authorizer '" + authorizer + "'")
                            .toString();
                default:
                    return "Error: Invalid action: " + action;
            }
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to manage authorization: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Configure mapping rules assigning roles to users, or retrieve the configured authenticator chains.
     */
    @McpTool(
            description = "Configure mapping rules assigning roles to users, or retrieve the configured authenticator chains. Parameters: [authorizer] (String, required), [action] (Enum: ASSIGN_ROLE, UNASSIGN_ROLE, GET_CHAIN, required), [username] (String, optional), and [roleName] (String, optional).",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, idempotentHint = false, destructiveHint = true)
    )
    public String manageSecurityAssignments(
            @McpToolParam(description = "Name of the authorizer (e.g., 'db')", required = true) String authorizer,
            @McpToolParam(description = "Action to perform: ASSIGN_ROLE, UNASSIGN_ROLE, GET_CHAIN", required = true) String action,
            @McpToolParam(description = "Username (optional/required depending on action)", required = false) String username,
            @McpToolParam(description = "Role name (optional/required depending on action)", required = false) String roleName
    ) {
        if (action == null) {
            return "Error: [action] parameter is required.";
        }
        String act = action.toUpperCase();
        try {
            switch (act) {
                case "ASSIGN_ROLE":
                    if (username == null || roleName == null) {
                        return "Error: [username] and [roleName] are required for ASSIGN_ROLE.";
                    }
                    druidBasicSecurityExtensionRepository.assignRoleToUser(authorizer, username, roleName);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "Role '" + roleName + "' assigned successfully to user '" + username + "' in authorizer '" + authorizer + "'")
                            .toString();
                case "UNASSIGN_ROLE":
                    if (username == null || roleName == null) {
                        return "Error: [username] and [roleName] are required for UNASSIGN_ROLE.";
                    }
                    druidBasicSecurityExtensionRepository.unassignRoleFromUser(authorizer, username, roleName);
                    return objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "Role '" + roleName + "' unassigned successfully from user '" + username + "' in authorizer '" + authorizer + "'")
                            .toString();
                case "GET_CHAIN":
                    JsonNode props = druidBasicSecurityExtensionRepository.getCoordinatorProperties();

                    String authChainStr = props.path("druid.auth.authenticatorChain").asText("");
                    String authorizersStr = props.path("druid.auth.authorizers").asText("");

                    ArrayNode authenticatorChain = objectMapper.createArrayNode();
                    ArrayNode authorizers = objectMapper.createArrayNode();

                    try {
                        if (authChainStr != null && !authChainStr.isEmpty()) {
                            JsonNode parsed = objectMapper.readTree(authChainStr);
                            if (parsed.isArray()) {
                                for (JsonNode n : parsed) {
                                    authenticatorChain.add(n.asText());
                                }
                            }
                        }
                    } catch (Exception ignore) {}

                    try {
                        if (authorizersStr != null && !authorizersStr.isEmpty()) {
                            JsonNode parsed = objectMapper.readTree(authorizersStr);
                            if (parsed.isArray()) {
                                for (JsonNode n : parsed) {
                                    authorizers.add(n.asText());
                                }
                            }
                        }
                    } catch (Exception ignore) {}

                    ObjectNode result = objectMapper.createObjectNode();
                    result.set("authenticatorChain", authenticatorChain);
                    result.set("authorizers", authorizers);
                    return result.toString();
                default:
                    return "Error: Invalid action: " + action;
            }
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to manage security assignments: " + e.getMessage())
                    .toString();
        }
    }
}
