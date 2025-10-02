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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthorizationTools
 */
class AuthorizationToolsTest {

    private SecurityRepository securityRepository;
    private ObjectMapper objectMapper;
    private AuthorizationTools authorizationTools;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG_LOG] Setting up AuthorizationToolsTest");
        securityRepository = mock(SecurityRepository.class);
        objectMapper = new ObjectMapper();
        authorizationTools = new AuthorizationTools(securityRepository, objectMapper);
    }

    @Test
    void listAuthorizationUsers_success() {
        System.out.println("[DEBUG_LOG] listAuthorizationUsers_success");
        ArrayNode arr = objectMapper.createArrayNode().add("u1").add("u2");
        when(securityRepository.getAllAuthorizationUsers("db")).thenReturn(arr);

        String result = authorizationTools.listAuthorizationUsers("db");
        assertTrue(result.contains("u1") && result.contains("u2"));
    }

    @Test
    void listAuthorizationUsers_error() {
        System.out.println("[DEBUG_LOG] listAuthorizationUsers_error");
        when(securityRepository.getAllAuthorizationUsers(anyString()))
                .thenThrow(new RestClientException("boom"));
        String result = authorizationTools.listAuthorizationUsers("db");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("boom"));
    }

    @Test
    void getAuthorizationUser_success() {
        System.out.println("[DEBUG_LOG] getAuthorizationUser_success");
        ObjectNode user = objectMapper.createObjectNode().put("name", "alice");
        when(securityRepository.getAuthorizationUser("db", "alice")).thenReturn(user);
        String result = authorizationTools.getAuthorizationUser("db", "alice");
        assertTrue(result.contains("alice"));
    }

    @Test
    void getAuthorizationUser_error() {
        System.out.println("[DEBUG_LOG] getAuthorizationUser_error");
        when(securityRepository.getAuthorizationUser("db", "missing"))
                .thenThrow(new RestClientException("404"));
        String result = authorizationTools.getAuthorizationUser("db", "missing");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("404"));
    }

    @Test
    void createAuthorizationUser_success() {
        System.out.println("[DEBUG_LOG] createAuthorizationUser_success");
        when(securityRepository.createAuthorizationUser("db", "neo"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));
        String result = authorizationTools.createAuthorizationUser("db", "neo");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("created successfully"));
    }

    @Test
    void createAuthorizationUser_error() {
        System.out.println("[DEBUG_LOG] createAuthorizationUser_error");
        when(securityRepository.createAuthorizationUser("db", "neo"))
                .thenThrow(new RestClientException("bad request"));
        String result = authorizationTools.createAuthorizationUser("db", "neo");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("bad request"));
    }

    @Test
    void deleteAuthorizationUser_success() {
        System.out.println("[DEBUG_LOG] deleteAuthorizationUser_success");
        when(securityRepository.deleteAuthorizationUser("db", "neo"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));
        String result = authorizationTools.deleteAuthorizationUser("db", "neo");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void deleteAuthorizationUser_error() {
        System.out.println("[DEBUG_LOG] deleteAuthorizationUser_error");
        when(securityRepository.deleteAuthorizationUser("db", "neo"))
                .thenThrow(new RestClientException("forbidden"));
        String result = authorizationTools.deleteAuthorizationUser("db", "neo");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("forbidden"));
    }

    @Test
    void listRoles_success() {
        System.out.println("[DEBUG_LOG] listRoles_success");
        ArrayNode roles = objectMapper.createArrayNode().add("admin").add("reader");
        when(securityRepository.getAllRoles("db")).thenReturn(roles);
        String result = authorizationTools.listRoles("db");
        assertTrue(result.contains("admin") && result.contains("reader"));
    }

    @Test
    void listRoles_error() {
        System.out.println("[DEBUG_LOG] listRoles_error");
        when(securityRepository.getAllRoles(anyString())).thenThrow(new RestClientException("err"));
        String result = authorizationTools.listRoles("db");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("err"));
    }

    @Test
    void getRole_success() {
        System.out.println("[DEBUG_LOG] getRole_success");
        ObjectNode role = objectMapper.createObjectNode().put("name", "admin");
        when(securityRepository.getRole("db", "admin")).thenReturn(role);
        String result = authorizationTools.getRole("db", "admin");
        assertTrue(result.contains("admin"));
    }

    @Test
    void getRole_error() {
        System.out.println("[DEBUG_LOG] getRole_error");
        when(securityRepository.getRole("db", "missing")).thenThrow(new RestClientException("404"));
        String result = authorizationTools.getRole("db", "missing");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("404"));
    }

    @Test
    void createRole_success() {
        System.out.println("[DEBUG_LOG] createRole_success");
        when(securityRepository.createRole("db", "writer")).thenReturn(objectMapper.createObjectNode());
        String result = authorizationTools.createRole("db", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("created successfully"));
    }

    @Test
    void createRole_error() {
        System.out.println("[DEBUG_LOG] createRole_error");
        when(securityRepository.createRole("db", "writer")).thenThrow(new RestClientException("conflict"));
        String result = authorizationTools.createRole("db", "writer");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("conflict"));
    }

    @Test
    void deleteRole_success() {
        System.out.println("[DEBUG_LOG] deleteRole_success");
        when(securityRepository.deleteRole("db", "writer")).thenReturn(objectMapper.createObjectNode());
        String result = authorizationTools.deleteRole("db", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void deleteRole_error() {
        System.out.println("[DEBUG_LOG] deleteRole_error");
        when(securityRepository.deleteRole("db", "writer")).thenThrow(new RestClientException("forbidden"));
        String result = authorizationTools.deleteRole("db", "writer");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("forbidden"));
    }

    @Test
    void setRolePermissions_success() {
        System.out.println("[DEBUG_LOG] setRolePermissions_success");
        when(securityRepository.setRolePermissions("db", "writer", "[]"))
                .thenReturn(objectMapper.createObjectNode());
        String result = authorizationTools.setRolePermissions("db", "writer", "[]");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("Permissions updated successfully for role 'writer' in authorizer 'db'"));
    }

    @Test
    void setRolePermissions_error() {
        System.out.println("[DEBUG_LOG] setRolePermissions_error");
        when(securityRepository.setRolePermissions("db", "writer", "[]"))
                .thenThrow(new RestClientException("invalid"));
        String result = authorizationTools.setRolePermissions("db", "writer", "[]");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("invalid"));
    }

    @Test
    void assignRoleToUser_success() {
        System.out.println("[DEBUG_LOG] assignRoleToUser_success");
        when(securityRepository.assignRoleToUser("db", "neo", "writer"))
                .thenReturn(objectMapper.createObjectNode());
        String result = authorizationTools.assignRoleToUser("db", "neo", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("assigned successfully to user"));
    }

    @Test
    void assignRoleToUser_error() {
        System.out.println("[DEBUG_LOG] assignRoleToUser_error");
        when(securityRepository.assignRoleToUser("db", "neo", "writer"))
                .thenThrow(new RestClientException("denied"));
        String result = authorizationTools.assignRoleToUser("db", "neo", "writer");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("denied"));
    }

    @Test
    void unassignRoleFromUser_success() {
        System.out.println("[DEBUG_LOG] unassignRoleFromUser_success");
        when(securityRepository.unassignRoleFromUser("db", "neo", "writer"))
                .thenReturn(objectMapper.createObjectNode());
        String result = authorizationTools.unassignRoleFromUser("db", "neo", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("unassigned successfully from user"));
    }

    @Test
    void unassignRoleFromUser_error() {
        System.out.println("[DEBUG_LOG] unassignRoleFromUser_error");
        when(securityRepository.unassignRoleFromUser("db", "neo", "writer"))
                .thenThrow(new RestClientException("oops"));
        String result = authorizationTools.unassignRoleFromUser("db", "neo", "writer");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("oops"));
    }
}
