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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DruidBasicSecurityExtensionTools
 */
class DruidBasicSecurityExtensionToolsTest {

    private DruidBasicSecurityExtensionRepository druidBasicSecurityExtensionRepository;
    private ObjectMapper objectMapper;
    private DruidBasicSecurityExtensionTools druidBasicSecurityExtensionTools;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG_LOG] Setting up DruidBasicSecurityExtensionToolsTest");
        druidBasicSecurityExtensionRepository = mock(DruidBasicSecurityExtensionRepository.class);
        objectMapper = new ObjectMapper();
        druidBasicSecurityExtensionTools = new DruidBasicSecurityExtensionTools(druidBasicSecurityExtensionRepository, objectMapper);
    }

    @Test
    void manageAuthentication_list_success() {
        System.out.println("[DEBUG_LOG] manageAuthentication_list_success");
        ArrayNode arr = objectMapper.createArrayNode();
        arr.add("alice");
        arr.add("bob");
        when(druidBasicSecurityExtensionRepository.getAllUsers("db")).thenReturn(arr);

        String result = druidBasicSecurityExtensionTools.manageAuthentication("db", "LIST", null, null);
        assertNotNull(result);
        assertTrue(result.contains("alice") && result.contains("bob"));
    }

    @Test
    void manageAuthentication_list_error() {
        System.out.println("[DEBUG_LOG] manageAuthentication_list_error");
        when(druidBasicSecurityExtensionRepository.getAllUsers(anyString())).thenThrow(new RestClientException("connection refused"));

        String result = druidBasicSecurityExtensionTools.manageAuthentication("db", "LIST", null, null);
        assertNotNull(result);
        assertTrue(result.contains("error"));
        assertTrue(result.contains("connection refused"));
    }

    @Test
    void manageAuthentication_get_success() {
        System.out.println("[DEBUG_LOG] manageAuthentication_get_success");
        ObjectNode user = objectMapper.createObjectNode();
        user.put("name", "alice");
        when(druidBasicSecurityExtensionRepository.getUser("db", "alice")).thenReturn(user);

        String result = druidBasicSecurityExtensionTools.manageAuthentication("db", "GET", "alice", null);
        assertTrue(result.contains("alice"));
    }

    @Test
    void manageAuthentication_create_success() {
        System.out.println("[DEBUG_LOG] manageAuthentication_create_success");
        when(druidBasicSecurityExtensionRepository.createUser("db", "dana"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = druidBasicSecurityExtensionTools.manageAuthentication("db", "CREATE", "dana", null);
        assertTrue(result.contains("success"));
        assertTrue(result.contains("created successfully"));
    }

    @Test
    void manageAuthentication_delete_success() {
        System.out.println("[DEBUG_LOG] manageAuthentication_delete_success");
        when(druidBasicSecurityExtensionRepository.deleteUser("db", "frank"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = druidBasicSecurityExtensionTools.manageAuthentication("db", "DELETE", "frank", null);
        assertTrue(result.contains("success"));
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void manageAuthentication_setPassword_success() {
        System.out.println("[DEBUG_LOG] manageAuthentication_setPassword_success");
        when(druidBasicSecurityExtensionRepository.setUserCredentials("db", "gina", "pw"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = druidBasicSecurityExtensionTools.manageAuthentication("db", "SET_PASSWORD", "gina", "pw");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("Password updated successfully"));
    }

    @Test
    void manageAuthorization_listUsers_success() {
        System.out.println("[DEBUG_LOG] manageAuthorization_listUsers_success");
        ArrayNode arr = objectMapper.createArrayNode().add("u1").add("u2");
        when(druidBasicSecurityExtensionRepository.getAllAuthorizationUsers("db")).thenReturn(arr);

        String result = druidBasicSecurityExtensionTools.manageAuthorization("db", "LIST_USERS", null, null);
        assertTrue(result.contains("u1") && result.contains("u2"));
    }

    @Test
    void manageAuthorization_listRoles_success() {
        System.out.println("[DEBUG_LOG] manageAuthorization_listRoles_success");
        ArrayNode roles = objectMapper.createArrayNode().add("admin").add("reader");
        when(druidBasicSecurityExtensionRepository.getAllRoles("db")).thenReturn(roles);

        String result = druidBasicSecurityExtensionTools.manageAuthorization("db", "LIST_ROLES", null, null);
        assertTrue(result.contains("admin") && result.contains("reader"));
    }

    @Test
    void manageAuthorization_setPermissions_success() {
        System.out.println("[DEBUG_LOG] manageAuthorization_setPermissions_success");
        when(druidBasicSecurityExtensionRepository.setRolePermissions("db", "writer", "[]"))
                .thenReturn(objectMapper.createObjectNode());

        String result = druidBasicSecurityExtensionTools.manageAuthorization("db", "SET_PERMISSIONS", "writer", "[]");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("Permissions updated successfully"));
    }

    @Test
    void manageSecurityAssignments_assign_success() {
        System.out.println("[DEBUG_LOG] manageSecurityAssignments_assign_success");
        when(druidBasicSecurityExtensionRepository.assignRoleToUser("db", "neo", "writer"))
                .thenReturn(objectMapper.createObjectNode());

        String result = druidBasicSecurityExtensionTools.manageSecurityAssignments("db", "ASSIGN_ROLE", "neo", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("assigned successfully to user"));
    }

    @Test
    void manageSecurityAssignments_getChain_success() {
        System.out.println("[DEBUG_LOG] manageSecurityAssignments_getChain_success");
        ObjectNode props = objectMapper.createObjectNode();
        props.put("druid.auth.authenticatorChain", "[\"db\"]");
        props.put("druid.auth.authorizers", "[\"db\"]");
        when(druidBasicSecurityExtensionRepository.getCoordinatorProperties()).thenReturn(props);

        String result = druidBasicSecurityExtensionTools.manageSecurityAssignments("db", "GET_CHAIN", null, null);
        assertTrue(result.contains("authenticatorChain"));
        assertTrue(result.contains("authorizers"));
    }
}
