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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WriteAuthorizationToolsTest {

    private SecurityRepository securityRepository;
    private ObjectMapper objectMapper;
    private WriteAuthorizationTools writeAuthorizationTools;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG_LOG] Setting up WriteAuthorizationToolsTest");
        securityRepository = mock(SecurityRepository.class);
        objectMapper = new ObjectMapper();
        writeAuthorizationTools = new WriteAuthorizationTools(securityRepository, objectMapper);
    }

    @Test
    void createAuthorizationUser_success() {
        System.out.println("[DEBUG_LOG] createAuthorizationUser_success");
        when(securityRepository.createAuthorizationUser("db", "neo"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));
        String result = writeAuthorizationTools.createAuthorizationUser("db", "neo");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("created successfully"));
    }

    @Test
    void createAuthorizationUser_error() {
        System.out.println("[DEBUG_LOG] createAuthorizationUser_error");
        when(securityRepository.createAuthorizationUser("db", "neo"))
                .thenThrow(new RestClientException("bad request"));
        String result = writeAuthorizationTools.createAuthorizationUser("db", "neo");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("bad request"));
    }

    @Test
    void deleteAuthorizationUser_success() {
        System.out.println("[DEBUG_LOG] deleteAuthorizationUser_success");
        when(securityRepository.deleteAuthorizationUser("db", "neo"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));
        String result = writeAuthorizationTools.deleteAuthorizationUser("db", "neo");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void deleteAuthorizationUser_error() {
        System.out.println("[DEBUG_LOG] deleteAuthorizationUser_error");
        when(securityRepository.deleteAuthorizationUser("db", "neo"))
                .thenThrow(new RestClientException("forbidden"));
        String result = writeAuthorizationTools.deleteAuthorizationUser("db", "neo");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("forbidden"));
    }

    @Test
    void createRole_success() {
        System.out.println("[DEBUG_LOG] createRole_success");
        when(securityRepository.createRole("db", "writer")).thenReturn(objectMapper.createObjectNode());
        String result = writeAuthorizationTools.createRole("db", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("created successfully"));
    }

    @Test
    void createRole_error() {
        System.out.println("[DEBUG_LOG] createRole_error");
        when(securityRepository.createRole("db", "writer")).thenThrow(new RestClientException("conflict"));
        String result = writeAuthorizationTools.createRole("db", "writer");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("conflict"));
    }

    @Test
    void deleteRole_success() {
        System.out.println("[DEBUG_LOG] deleteRole_success");
        when(securityRepository.deleteRole("db", "writer")).thenReturn(objectMapper.createObjectNode());
        String result = writeAuthorizationTools.deleteRole("db", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void deleteRole_error() {
        System.out.println("[DEBUG_LOG] deleteRole_error");
        when(securityRepository.deleteRole("db", "writer")).thenThrow(new RestClientException("forbidden"));
        String result = writeAuthorizationTools.deleteRole("db", "writer");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("forbidden"));
    }

    @Test
    void setRolePermissions_success() {
        System.out.println("[DEBUG_LOG] setRolePermissions_success");
        when(securityRepository.setRolePermissions("db", "writer", "[]"))
                .thenReturn(objectMapper.createObjectNode());
        String result = writeAuthorizationTools.setRolePermissions("db", "writer", "[]");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("Permissions updated successfully for role 'writer' in authorizer 'db'"));
    }

    @Test
    void setRolePermissions_error() {
        System.out.println("[DEBUG_LOG] setRolePermissions_error");
        when(securityRepository.setRolePermissions("db", "writer", "[]"))
                .thenThrow(new RestClientException("invalid"));
        String result = writeAuthorizationTools.setRolePermissions("db", "writer", "[]");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("invalid"));
    }

    @Test
    void assignRoleToUser_success() {
        System.out.println("[DEBUG_LOG] assignRoleToUser_success");
        when(securityRepository.assignRoleToUser("db", "neo", "writer"))
                .thenReturn(objectMapper.createObjectNode());
        String result = writeAuthorizationTools.assignRoleToUser("db", "neo", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("assigned successfully to user"));
    }

    @Test
    void assignRoleToUser_error() {
        System.out.println("[DEBUG_LOG] assignRoleToUser_error");
        when(securityRepository.assignRoleToUser("db", "neo", "writer"))
                .thenThrow(new RestClientException("denied"));
        String result = writeAuthorizationTools.assignRoleToUser("db", "neo", "writer");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("denied"));
    }

    @Test
    void unassignRoleFromUser_success() {
        System.out.println("[DEBUG_LOG] unassignRoleFromUser_success");
        when(securityRepository.unassignRoleFromUser("db", "neo", "writer"))
                .thenReturn(objectMapper.createObjectNode());
        String result = writeAuthorizationTools.unassignRoleFromUser("db", "neo", "writer");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("unassigned successfully from user"));
    }

    @Test
    void unassignRoleFromUser_error() {
        System.out.println("[DEBUG_LOG] unassignRoleFromUser_error");
        when(securityRepository.unassignRoleFromUser("db", "neo", "writer"))
                .thenThrow(new RestClientException("oops"));
        String result = writeAuthorizationTools.unassignRoleFromUser("db", "neo", "writer");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("oops"));
    }
}
