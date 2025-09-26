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

package com.iunera.druidmcpserver.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=https://test-router:8888",
        "druid.auth.username=testuser",
        "druid.auth.password=testpass",
        "druid.ssl.enabled=true",
        "druid.ssl.skip-verification=true"
})
class DruidRestClientConfigSslSkipTest {

    @Autowired
    private DruidRestClientConfig druidRestClientConfig;

    @Autowired
    private RestClient druidRouterRestClient;

    @Test
    void testSslSkipVerificationEnabled() {
        System.out.println("[DEBUG_LOG] Testing SSL skip verification configuration");

        assertNotNull(druidRestClientConfig, "DruidRestClientConfig should be autowired");
        assertTrue(druidRestClientConfig.isSslEnabled(), "SSL should be enabled");
        assertTrue(druidRestClientConfig.isSkipSslVerification(), "SSL verification should be skipped");

        System.out.println("[DEBUG_LOG] SSL skip verification configuration verified");
    }

    @Test
    void testBasicAuthConfiguration() {
        System.out.println("[DEBUG_LOG] Testing basic authentication configuration");

        assertEquals("testuser", druidRestClientConfig.getDruidUsername());
        assertEquals("testpass", druidRestClientConfig.getDruidPassword());

        System.out.println("[DEBUG_LOG] Basic authentication configuration verified");
    }

    @Test
    void testRestClientWithSslSkipAndAuth() {
        System.out.println("[DEBUG_LOG] Testing RestClient creation with SSL skip and auth");

        assertNotNull(druidRouterRestClient, "Druid router RestClient should be created");

        // Verify that the RestClient was created successfully even with SSL skip verification
        // This confirms that the SSL context configuration is working
        System.out.println("[DEBUG_LOG] RestClient created successfully with SSL skip verification and basic auth");
    }
}
