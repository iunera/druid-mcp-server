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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReadAuthenticationTools
 */
class ReadAuthenticationToolsTest {

    private SecurityRepository securityRepository;
    private HealthStatusRepository healthStatusRepository;
    private ObjectMapper objectMapper;

    private ReadAuthenticationTools readAuthenticationTools;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG_LOG] Setting up ReadAuthenticationToolsTest");
        securityRepository = mock(SecurityRepository.class);
        healthStatusRepository = mock(HealthStatusRepository.class);
        objectMapper = new ObjectMapper();
        readAuthenticationTools = new ReadAuthenticationTools(securityRepository, healthStatusRepository, objectMapper);
    }

    @Test
    void listAuthenticationUsers_success() {
        System.out.println("[DEBUG_LOG] listAuthenticationUsers_success");
        ArrayNode arr = objectMapper.createArrayNode();
        arr.add("alice");
        arr.add("bob");
        when(securityRepository.getAllUsers("db")).thenReturn(arr);

        String result = readAuthenticationTools.listAuthenticationUsers("db");
        assertNotNull(result);
        assertTrue(result.contains("alice") && result.contains("bob"));
    }

    @Test
    void listAuthenticationUsers_error() {
        System.out.println("[DEBUG_LOG] listAuthenticationUsers_error");
        when(securityRepository.getAllUsers(anyString())).thenThrow(new RestClientException("connection refused"));

        String result = readAuthenticationTools.listAuthenticationUsers("db");
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

        String result = readAuthenticationTools.getAuthenticationUser("db", "alice");
        assertTrue(result.contains("alice"));
    }

    @Test
    void getAuthenticationUser_error() {
        System.out.println("[DEBUG_LOG] getAuthenticationUser_error");
        when(securityRepository.getUser(Mockito.eq("db"), Mockito.eq("charlie")))
                .thenThrow(new RestClientException("not found"));

        String result = readAuthenticationTools.getAuthenticationUser("db", "charlie");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("not found"));
    }
}