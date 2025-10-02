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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityConfigurationTools
 */
class SecurityConfigurationToolsTest {

    private ObjectMapper objectMapper;
    private SecurityRepository securityRepository;
    private HealthStatusRepository healthStatusRepository;
    private SecurityConfigurationTools tools;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG_LOG] Setting up SecurityConfigurationToolsTest");
        objectMapper = new ObjectMapper();
        securityRepository = mock(SecurityRepository.class);
        healthStatusRepository = mock(HealthStatusRepository.class);
        tools = new SecurityConfigurationTools(healthStatusRepository, objectMapper, securityRepository);
    }

    @Test
    void getAuthenticatorChainAndAuthorizers_success() {
        System.out.println("[DEBUG_LOG] getAuthenticatorChainAndAuthorizers_success");
        ObjectNode props = objectMapper.createObjectNode();
        props.put("druid.auth.authenticatorChain", "[\"basic\"]");
        props.put("druid.auth.authorizers", "[\"basic-authorizer\", \"another\"]");
        when(securityRepository.getCoordinatorProperties()).thenReturn(props);

        String result = tools.getAuthenticatorChainAndAuthorizers();
        assertNotNull(result);
        assertTrue(result.contains("authenticatorChain"));
        assertTrue(result.contains("basic"));
        assertTrue(result.contains("authorizers"));
        assertTrue(result.contains("basic-authorizer"));
        assertTrue(result.contains("another"));
    }

    @Test
    void getAuthenticatorChainAndAuthorizers_handlesMalformedJson() {
        System.out.println("[DEBUG_LOG] getAuthenticatorChainAndAuthorizers_handlesMalformedJson");
        ObjectNode props = objectMapper.createObjectNode();
        // malformed JSON strings
        props.put("druid.auth.authenticatorChain", "[invalid");
        props.put("druid.auth.authorizers", "");
        when(securityRepository.getCoordinatorProperties()).thenReturn(props);

        String result = tools.getAuthenticatorChainAndAuthorizers();
        assertNotNull(result);
        // Should still return JSON with arrays present (may be empty due to parse failure)
        assertTrue(result.contains("authenticatorChain"));
        assertTrue(result.contains("authorizers"));
    }

    @Test
    void getAuthenticatorChainAndAuthorizers_error() {
        System.out.println("[DEBUG_LOG] getAuthenticatorChainAndAuthorizers_error");
        when(securityRepository.getCoordinatorProperties()).thenThrow(new RestClientException("timeout"));

        String result = tools.getAuthenticatorChainAndAuthorizers();
        assertTrue(result.contains("error"));
        assertTrue(result.contains("timeout"));
    }
}
