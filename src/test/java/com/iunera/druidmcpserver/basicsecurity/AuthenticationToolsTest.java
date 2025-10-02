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
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthenticationTools
 */
class AuthenticationToolsTest {

    private SecurityRepository securityRepository;
    private HealthStatusRepository healthStatusRepository;
    private ObjectMapper objectMapper;

    private AuthenticationTools authenticationTools;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG_LOG] Setting up AuthenticationToolsTest");
        securityRepository = mock(SecurityRepository.class);
        healthStatusRepository = mock(HealthStatusRepository.class);
        objectMapper = new ObjectMapper();
        authenticationTools = new AuthenticationTools(securityRepository, healthStatusRepository, objectMapper);
    }

    @Test
    void listAuthenticationUsers_success() {
        System.out.println("[DEBUG_LOG] listAuthenticationUsers_success");
        ArrayNode arr = objectMapper.createArrayNode();
        arr.add("alice");
        arr.add("bob");
        when(securityRepository.getAllUsers("db")).thenReturn(arr);

        String result = authenticationTools.listAuthenticationUsers("db");
        assertNotNull(result);
        assertTrue(result.contains("alice") && result.contains("bob"));
    }

    @Test
    void listAuthenticationUsers_error() {
        System.out.println("[DEBUG_LOG] listAuthenticationUsers_error");
        when(securityRepository.getAllUsers(anyString())).thenThrow(new RestClientException("connection refused"));

        String result = authenticationTools.listAuthenticationUsers("db");
        assertNotNull(result);
        assertTrue(result.contains("error"));
        assertTrue(result.contains("connection refused"));
    }

    @Test
    void getAuthenticationUser_success() {
        System.out.println("[DEBUG_LOG] getAuthenticationUser_success");
        ObjectNode user = objectMapper.createObjectNode();
        user.put("name", "alice");
        when(securityRepository.getUser("db", "alice")).thenReturn(user);

        String result = authenticationTools.getAuthenticationUser("db", "alice");
        assertTrue(result.contains("alice"));
    }

    @Test
    void getAuthenticationUser_error() {
        System.out.println("[DEBUG_LOG] getAuthenticationUser_error");
        when(securityRepository.getUser(Mockito.eq("db"), Mockito.eq("charlie")))
                .thenThrow(new RestClientException("not found"));

        String result = authenticationTools.getAuthenticationUser("db", "charlie");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("not found"));
    }

    @Test
    void createAuthenticationUser_success() {
        System.out.println("[DEBUG_LOG] createAuthenticationUser_success");
        when(securityRepository.createUser("db", "dana"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = authenticationTools.createAuthenticationUser("db", "dana");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("created successfully"));
    }

    @Test
    void createAuthenticationUser_error() {
        System.out.println("[DEBUG_LOG] createAuthenticationUser_error");
        when(securityRepository.createUser("db", "ed"))
                .thenThrow(new RestClientException("bad request"));

        String result = authenticationTools.createAuthenticationUser("db", "ed");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("bad request"));
    }

    @Test
    void deleteAuthenticationUser_success() {
        System.out.println("[DEBUG_LOG] deleteAuthenticationUser_success");
        when(securityRepository.deleteUser("db", "frank"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = authenticationTools.deleteAuthenticationUser("db", "frank");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void deleteAuthenticationUser_error() {
        System.out.println("[DEBUG_LOG] deleteAuthenticationUser_error");
        when(securityRepository.deleteUser("db", "frank"))
                .thenThrow(new RestClientException("conflict"));

        String result = authenticationTools.deleteAuthenticationUser("db", "frank");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("conflict"));
    }

    @Test
    void setUserPassword_success() {
        System.out.println("[DEBUG_LOG] setUserPassword_success");
        when(securityRepository.setUserCredentials("db", "gina", "pw"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = authenticationTools.setUserPassword("db", "gina", "pw");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("Password updated successfully"));
    }

    @Test
    void setUserPassword_error() {
        System.out.println("[DEBUG_LOG] setUserPassword_error");
        when(securityRepository.setUserCredentials("db", "gina", "pw"))
                .thenThrow(new RestClientException("unauthorized"));

        String result = authenticationTools.setUserPassword("db", "gina", "pw");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("unauthorized"));
    }
}
