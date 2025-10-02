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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReadAuthorizationTools
 */
class ReadAuthorizationToolsTest {

    private SecurityRepository securityRepository;
    private ObjectMapper objectMapper;
    private ReadAuthorizationTools readAuthorizationTools;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG_LOG] Setting up ReadAuthorizationToolsTest");
        securityRepository = mock(SecurityRepository.class);
        objectMapper = new ObjectMapper();
        readAuthorizationTools = new ReadAuthorizationTools(securityRepository, objectMapper);
    }

    @Test
    void listAuthorizationUsers_success() {
        System.out.println("[DEBUG_LOG] listAuthorizationUsers_success");
        ArrayNode arr = objectMapper.createArrayNode().add("u1").add("u2");
        when(securityRepository.getAllAuthorizationUsers("db")).thenReturn(arr);

        String result = readAuthorizationTools.listAuthorizationUsers("db");
        assertTrue(result.contains("u1") && result.contains("u2"));
    }

    @Test
    void listAuthorizationUsers_error() {
        System.out.println("[DEBUG_LOG] listAuthorizationUsers_error");
        when(securityRepository.getAllAuthorizationUsers(anyString()))
                .thenThrow(new RestClientException("boom"));
        String result = readAuthorizationTools.listAuthorizationUsers("db");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("boom"));
    }

    @Test
    void getAuthorizationUser_success() {
        System.out.println("[DEBUG_LOG] getAuthorizationUser_success");
        ObjectNode user = objectMapper.createObjectNode().put("name", "alice");
        when(securityRepository.getAuthorizationUser("db", "alice")).thenReturn(user);
        String result = readAuthorizationTools.getAuthorizationUser("db", "alice");
        assertTrue(result.contains("alice"));
    }

    @Test
    void getAuthorizationUser_error() {
        System.out.println("[DEBUG_LOG] getAuthorizationUser_error");
        when(securityRepository.getAuthorizationUser("db", "missing"))
                .thenThrow(new RestClientException("404"));
        String result = readAuthorizationTools.getAuthorizationUser("db", "missing");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("404"));
    }

    @Test
    void listRoles_success() {
        System.out.println("[DEBUG_LOG] listRoles_success");
        ArrayNode roles = objectMapper.createArrayNode().add("admin").add("reader");
        when(securityRepository.getAllRoles("db")).thenReturn(roles);
        String result = readAuthorizationTools.listRoles("db");
        assertTrue(result.contains("admin") && result.contains("reader"));
    }

    @Test
    void listRoles_error() {
        System.out.println("[DEBUG_LOG] listRoles_error");
        when(securityRepository.getAllRoles(anyString())).thenThrow(new RestClientException("err"));
        String result = readAuthorizationTools.listRoles("db");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("err"));
    }

    @Test
    void getRole_success() {
        System.out.println("[DEBUG_LOG] getRole_success");
        ObjectNode role = objectMapper.createObjectNode().put("name", "admin");
        when(securityRepository.getRole("db", "admin")).thenReturn(role);
        String result = readAuthorizationTools.getRole("db", "admin");
        assertTrue(result.contains("admin"));
    }

    @Test
    void getRole_error() {
        System.out.println("[DEBUG_LOG] getRole_error");
        when(securityRepository.getRole("db", "missing")).thenThrow(new RestClientException("404"));
        String result = readAuthorizationTools.getRole("db", "missing");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("404"));
    }
}