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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Authentication Tool Provider for Druid MCP Server
 * Provides authentication-related tools for user management in Druid basic security
 */
@Component
public class AuthenticationTools {

    private final SecurityRepository securityRepository;
    private final HealthStatusRepository healthStatusRepository;
    private final ObjectMapper objectMapper;

    public AuthenticationTools(SecurityRepository securityRepository, HealthStatusRepository healthStatusRepository, ObjectMapper objectMapper) {
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

    /**
     * Create a new user in the authentication system
     */
    @McpTool(description = "Create a new user in the Druid authentication system")
    public String createAuthenticationUser(
            @McpToolParam(description = "Name of the authenticator (e.g., 'db')", required = true) 
            String authenticatorName,
            @McpToolParam(description = "Username to create", required = true) 
            String userName) {
        try {
            JsonNode result = securityRepository.createUser(authenticatorName, userName);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "User '" + userName + "' created successfully in authenticator '" + authenticatorName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to create authentication user: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Delete a user from the authentication system
     */
    @McpTool(description = "Delete a user from the Druid authentication system. Use with caution as this action cannot be undone.")
    public String deleteAuthenticationUser(
            @McpToolParam(description = "Name of the authenticator (e.g., 'db')", required = true) 
            String authenticatorName,
            @McpToolParam(description = "Username to delete", required = true) 
            String userName) {
        try {
            JsonNode result = securityRepository.deleteUser(authenticatorName, userName);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "User '" + userName + "' deleted successfully from authenticator '" + authenticatorName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to delete authentication user: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Set user password in the authentication system
     */
    @McpTool(description = "Set or update the password for a user in the Druid authentication system")
    public String setUserPassword(
            @McpToolParam(description = "Name of the authenticator (e.g., 'db')", required = true) 
            String authenticatorName,
            @McpToolParam(description = "Username for password update", required = true) 
            String userName,
            @McpToolParam(description = "New password for the user", required = true) 
            String password) {
        try {
            JsonNode result = securityRepository.setUserCredentials(authenticatorName, userName, password);
            return objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "Password updated successfully for user '" + userName + "' in authenticator '" + authenticatorName + "'")
                    .toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to set user password: " + e.getMessage())
                    .toString();
        }
    }

    /**
     * Get configured authenticator chain and authorizers from coordinator properties
     */
    @McpTool(description = "Get configured authenticatorChain and authorizers from Druid coordinator properties")
    public String getauthenticatorChainAndAuthorizers() {
        try {
            JsonNode props = healthStatusRepository.getCoordinatorProperties();

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
            } catch (Exception ignore) {
                // ignore parsing errors and return empty or partially filled result
            }

            try {
                if (authorizersStr != null && !authorizersStr.isEmpty()) {
                    JsonNode parsed = objectMapper.readTree(authorizersStr);
                    if (parsed.isArray()) {
                        for (JsonNode n : parsed) {
                            authorizers.add(n.asText());
                        }
                    }
                }
            } catch (Exception ignore) {
                // ignore parsing errors
            }

            ObjectNode result = objectMapper.createObjectNode();
            result.set("authenticatorChain", authenticatorChain);
            result.set("authorizers", authorizers);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get coordinator properties: " + e.getMessage())
                    .toString();
        }
    }
}