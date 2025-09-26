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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for environment variable style configuration via relaxed binding
 */
@SpringBootTest
@ContextConfiguration(initializers = DruidRestClientConfigEnvTest.EnvInitializer.class)
class DruidRestClientConfigEnvTest {

    @Autowired
    private DruidRestClientConfig druidRestClientConfig;

    @Test
    void testEnvironmentVariableConfiguration() {
        System.out.println("[DEBUG_LOG] Testing environment variable configuration via SystemEnvironmentPropertySource");

        assertNotNull(druidRestClientConfig, "DruidRestClientConfig should be autowired");

        // Verify that environment-style variables are properly loaded through relaxed binding
        assertEquals("test-user", druidRestClientConfig.getDruidUsername());
        assertEquals("test-password", druidRestClientConfig.getDruidPassword());
        assertEquals("https://druid.example.com", druidRestClientConfig.getDruidRouterUrl());
        assertTrue(druidRestClientConfig.isSslEnabled());
        assertFalse(druidRestClientConfig.isSkipSslVerification());

        System.out.println("[DEBUG_LOG] Username from env: " + druidRestClientConfig.getDruidUsername());
        System.out.println("[DEBUG_LOG] Password from env: " + druidRestClientConfig.getDruidPassword());

        System.out.println("[DEBUG_LOG] Environment variable configuration test completed successfully");
    }

    static class EnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            Map<String, Object> env = new HashMap<>();
            env.put("DRUID_AUTH_USERNAME", "test-user");
            env.put("DRUID_AUTH_PASSWORD", "test-password");
            env.put("DRUID_ROUTER_URL", "https://druid.example.com");
            env.put("DRUID_SSL_ENABLED", "true");
            env.put("DRUID_SSL_SKIP_VERIFICATION", "false");
            applicationContext.getEnvironment().getPropertySources()
                    .addFirst(new SystemEnvironmentPropertySource("test-env", env));
        }
    }
}
