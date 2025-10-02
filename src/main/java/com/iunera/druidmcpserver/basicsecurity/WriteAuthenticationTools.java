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
@Component
public class WriteAuthenticationTools {

    private final SecurityRepository securityRepository;
    private final ObjectMapper objectMapper;

    public WriteAuthenticationTools(SecurityRepository securityRepository, ObjectMapper objectMapper) {
        this.securityRepository = securityRepository;
        this.objectMapper = objectMapper;
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

}
