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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Repository for Druid Security API operations
 * Handles both authentication and authorization API calls
 */
@Repository
public class SecurityRepository {

    private final RestClient druidCoordinatorRestClient;

    public SecurityRepository(@Qualifier("druidCoordinatorRestClient") RestClient druidCoordinatorRestClient) {
        this.druidCoordinatorRestClient = druidCoordinatorRestClient;
    }

    /**
     * Ensures that the given role is not the protected 'admin' role.
     * Throws IllegalArgumentException if an attempt is made to modify the admin role.
     */
    private void ensureMutableRoles(String roleName) {
        if ("admin".equalsIgnoreCase(roleName.trim()) || "druid_system".equalsIgnoreCase(roleName.trim())) {
            throw new IllegalArgumentException("The " + roleName + " role cannot be modified.");
        }
    }

    /**
     * Get coordinator properties
     */
    public JsonNode getCoordinatorProperties() throws RestClientException {
        return druidCoordinatorRestClient
                .get()
                .uri("/status/properties")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }


    // Authentication API methods

    /**
     * Get all users from the authentication system
     */
    public JsonNode getAllUsers(String authenticatorName) throws RestClientException {
        return druidCoordinatorRestClient
                .get()
                .uri("/druid-ext/basic-security/authentication/db/{authenticatorName}/users", authenticatorName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get specific user from the authentication system
     */
    public JsonNode getUser(String authenticatorName, String userName) throws RestClientException {
        return druidCoordinatorRestClient
                .get()
                .uri("/druid-ext/basic-security/authentication/db/{authenticatorName}/users/{userName}",
                        authenticatorName, userName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Create a user in the authentication system
     */
    public JsonNode createUser(String authenticatorName, String userName) throws RestClientException {
        return druidCoordinatorRestClient
                .post()
                .uri("/druid-ext/basic-security/authentication/db/{authenticatorName}/users/{userName}",
                        authenticatorName, userName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Delete a user from the authentication system
     */
    public JsonNode deleteUser(String authenticatorName, String userName) throws RestClientException {
        return druidCoordinatorRestClient
                .delete()
                .uri("/druid-ext/basic-security/authentication/db/{authenticatorName}/users/{userName}",
                        authenticatorName, userName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Set user credentials (password)
     */
    public JsonNode setUserCredentials(String authenticatorName, String userName, String password) throws RestClientException {
        String body = "{\"password\": \"" + password + "\"}";
        return druidCoordinatorRestClient
                .post()
                .uri("/druid-ext/basic-security/authentication/db/{authenticatorName}/users/{userName}/credentials",
                        authenticatorName, userName)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    // Authorization API methods

    /**
     * Get all users from the authorization system
     */
    public JsonNode getAllAuthorizationUsers(String authorizerName) throws RestClientException {
        return druidCoordinatorRestClient
                .get()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/users", authorizerName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get specific user from the authorization system
     */
    public JsonNode getAuthorizationUser(String authorizerName, String userName) throws RestClientException {
        return druidCoordinatorRestClient
                .get()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/users/{userName}",
                        authorizerName, userName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Create a user in the authorization system
     */
    public JsonNode createAuthorizationUser(String authorizerName, String userName) throws RestClientException {
        return druidCoordinatorRestClient
                .post()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/users/{userName}",
                        authorizerName, userName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Delete a user from the authorization system
     */
    public JsonNode deleteAuthorizationUser(String authorizerName, String userName) throws RestClientException {
        return druidCoordinatorRestClient
                .delete()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/users/{userName}",
                        authorizerName, userName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get all roles
     */
    public JsonNode getAllRoles(String authorizerName) throws RestClientException {
        return druidCoordinatorRestClient
                .get()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/roles", authorizerName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get specific role
     */
    public JsonNode getRole(String authorizerName, String roleName) throws RestClientException {
        return druidCoordinatorRestClient
                .get()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/roles/{roleName}",
                        authorizerName, roleName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Create a role
     */
    public JsonNode createRole(String authorizerName, String roleName) throws RestClientException {
        return druidCoordinatorRestClient
                .post()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/roles/{roleName}",
                        authorizerName, roleName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Delete a role
     */
    public JsonNode deleteRole(String authorizerName, String roleName) throws RestClientException {
        ensureMutableRoles(roleName);
        return druidCoordinatorRestClient
                .delete()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/roles/{roleName}",
                        authorizerName, roleName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Set role permissions
     */
    public JsonNode setRolePermissions(String authorizerName, String roleName, String permissions) throws RestClientException {
        ensureMutableRoles(roleName);
        return druidCoordinatorRestClient
                .post()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/roles/{roleName}/permissions",
                        authorizerName, roleName)
                .header("Content-Type", "application/json")
                .body(permissions)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Assign role to user
     */
    public JsonNode assignRoleToUser(String authorizerName, String userName, String roleName) throws RestClientException {
        return druidCoordinatorRestClient
                .post()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/users/{userName}/roles/{roleName}",
                        authorizerName, userName, roleName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Unassign role from user
     */
    public JsonNode unassignRoleFromUser(String authorizerName, String userName, String roleName) throws RestClientException {
        return druidCoordinatorRestClient
                .delete()
                .uri("/druid-ext/basic-security/authorization/db/{authorizerName}/users/{userName}/roles/{roleName}",
                        authorizerName, userName, roleName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}