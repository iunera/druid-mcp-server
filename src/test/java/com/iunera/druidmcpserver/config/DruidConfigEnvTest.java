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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for environment variable configuration
 */
@SpringBootTest
@TestPropertySource(properties = {
        "DRUID_AUTH_USERNAME=test-user",
        "DRUID_AUTH_PASSWORD=test-password",
        "DRUID_ROUTER_URL=https://druid.example.com",
        "DRUID_SSL_ENABLED=true",
        "DRUID_SSL_SKIP_VERIFICATION=false"
})
class DruidConfigEnvTest {

    @Autowired
    private DruidConfig druidConfig;

    @Test
    void testEnvironmentVariableConfiguration() {
        System.out.println("[DEBUG_LOG] Testing environment variable configuration");

        assertNotNull(druidConfig, "DruidConfig should be autowired");

        // Verify that environment variables are properly loaded
        assertEquals("test-user", druidConfig.getDruidUsername());
        assertEquals("test-password", druidConfig.getDruidPassword());
        assertEquals("https://druid.example.com", druidConfig.getDruidRouterUrl());
        assertTrue(druidConfig.isSslEnabled());
        assertFalse(druidConfig.isSkipSslVerification());


        System.out.println("[DEBUG_LOG] Username from env: " + druidConfig.getDruidUsername());
        System.out.println("[DEBUG_LOG] Password from env: " + druidConfig.getDruidPassword());

        System.out.println("[DEBUG_LOG] Environment variable configuration test completed successfully");
    }
}
