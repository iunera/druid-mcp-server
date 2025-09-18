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

@SpringBootTest
class ReadonlyModePropertiesTest {

    @Autowired
    private ReadonlyModeProperties readonlyModeProperties;

    @Test
    void defaultIsDisabled() {
        System.out.println("[DEBUG_LOG] Testing default read-only property disabled");
        assertNotNull(readonlyModeProperties);
        assertFalse(readonlyModeProperties.isEnabled(), "Read-only should be disabled by default");
    }

    @SpringBootTest
    @TestPropertySource(properties = {
            "druid.mcp.readonly.enabled=true"
    })
    static class EnabledProfileTest {
        @Autowired
        private ReadonlyModeProperties props;

        @Test
        void propertyBindsToTrue() {
            System.out.println("[DEBUG_LOG] Testing read-only property enabled=true");
            assertNotNull(props);
            assertTrue(props.isEnabled(), "Read-only should be enabled when property is set to true");
        }
    }
}
