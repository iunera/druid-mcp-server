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
        "druid.ssl.skip-verification=false"
})
class DruidConfigTest {

    @Autowired
    private DruidConfig druidConfig;

    @Autowired
    private RestClient druidRouterRestClient;

    @Test
    void testDruidConfigPropertiesLoaded() {
        System.out.println("[DEBUG_LOG] Testing DruidConfig properties loading");

        assertNotNull(druidConfig, "DruidConfig should be autowired");
        assertEquals("https://test-router:8888", druidConfig.getDruidRouterUrl());
        assertEquals("testuser", druidConfig.getDruidUsername());
        assertEquals("testpass", druidConfig.getDruidPassword());
        assertTrue(druidConfig.isSslEnabled());
        assertFalse(druidConfig.isSkipSslVerification());

        System.out.println("[DEBUG_LOG] All DruidConfig properties loaded correctly");
    }

    @Test
    void testRestClientBeansCreated() {
        System.out.println("[DEBUG_LOG] Testing RestClient beans creation");

        assertNotNull(druidRouterRestClient, "Druid router RestClient should be created");

        System.out.println("[DEBUG_LOG] RestClient beans created successfully");
    }
}
