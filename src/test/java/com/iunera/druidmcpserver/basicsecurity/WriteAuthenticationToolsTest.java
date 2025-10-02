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

class WriteAuthenticationToolsTest {

    private SecurityRepository securityRepository;
    private ObjectMapper objectMapper;

    private WriteAuthenticationTools writeAuthenticationTools;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG_LOG] Setting up WriteAuthenticationToolsTest");
        securityRepository = mock(SecurityRepository.class);
        objectMapper = new ObjectMapper();
        writeAuthenticationTools = new WriteAuthenticationTools(securityRepository, objectMapper);
    }

    @Test
    void createAuthenticationUser_success() {
        System.out.println("[DEBUG_LOG] createAuthenticationUser_success");
        when(securityRepository.createUser("db", "dana"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = writeAuthenticationTools.createAuthenticationUser("db", "dana");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("created successfully"));
    }

    @Test
    void createAuthenticationUser_error() {
        System.out.println("[DEBUG_LOG] createAuthenticationUser_error");
        when(securityRepository.createUser("db", "ed"))
                .thenThrow(new RestClientException("bad request"));

        String result = writeAuthenticationTools.createAuthenticationUser("db", "ed");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("bad request"));
    }

    @Test
    void deleteAuthenticationUser_success() {
        System.out.println("[DEBUG_LOG] deleteAuthenticationUser_success");
        when(securityRepository.deleteUser("db", "frank"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = writeAuthenticationTools.deleteAuthenticationUser("db", "frank");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void deleteAuthenticationUser_error() {
        System.out.println("[DEBUG_LOG] deleteAuthenticationUser_error");
        when(securityRepository.deleteUser("db", "frank"))
                .thenThrow(new RestClientException("conflict"));

        String result = writeAuthenticationTools.deleteAuthenticationUser("db", "frank");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("conflict"));
    }

    @Test
    void setUserPassword_success() {
        System.out.println("[DEBUG_LOG] setUserPassword_success");
        when(securityRepository.setUserCredentials("db", "gina", "pw"))
                .thenReturn(objectMapper.createObjectNode().put("ok", true));

        String result = writeAuthenticationTools.setUserPassword("db", "gina", "pw");
        assertTrue(result.contains("success"));
        assertTrue(result.contains("Password updated successfully"));
    }

    @Test
    void setUserPassword_error() {
        System.out.println("[DEBUG_LOG] setUserPassword_error");
        when(securityRepository.setUserCredentials("db", "gina", "pw"))
                .thenThrow(new RestClientException("unauthorized"));

        String result = writeAuthenticationTools.setUserPassword("db", "gina", "pw");
        assertTrue(result.contains("error"));
        assertTrue(result.contains("unauthorized"));
    }
}
