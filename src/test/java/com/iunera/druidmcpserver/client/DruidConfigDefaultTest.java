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

package com.iunera.druidmcpserver.client;

import com.iunera.druidmcpserver.config.DruidConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for default configuration without environment variable overrides
 */
@SpringBootTest
class DruidConfigDefaultTest {

    @Autowired
    private DruidConfig druidConfig;

    @Test
    void testDefaultConfiguration() {
        System.out.println("[DEBUG_LOG] Testing that other configuration remains default");

        assertNotNull(druidConfig, "DruidConfig should be autowired");

        // Verify default values for other properties
        assertEquals("http://localhost:8888", druidConfig.getDruidRouterUrl());
        assertFalse(druidConfig.isSslEnabled());
        assertFalse(druidConfig.isSkipSslVerification());

        System.out.println("[DEBUG_LOG] Default configuration test completed successfully");
    }
}