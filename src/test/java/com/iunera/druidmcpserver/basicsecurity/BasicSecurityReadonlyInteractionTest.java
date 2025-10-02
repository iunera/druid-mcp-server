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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verify interaction with readonly mode: when readonly is enabled, write tools are absent
 * while read tools remain present (assuming the basic security feature flag is enabled).
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.extension.druid-basic-security.enabled=true",
        "druid.mcp.readonly.enabled=true"
})
class BasicSecurityReadonlyInteractionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void readonlyBlocksWriteToolsButKeepsReadTools() {
        System.out.println("[DEBUG_LOG] Verifying readonly interaction with basic security tools");

        // read beans should be present
        assertFalse(applicationContext.getBeansOfType(ReadAuthenticationTools.class).isEmpty(),
                "ReadAuthenticationTools should be present when feature enabled");
        assertFalse(applicationContext.getBeansOfType(ReadAuthorizationTools.class).isEmpty(),
                "ReadAuthorizationTools should be present when feature enabled");

        // write beans should be absent in readonly
        assertTrue(applicationContext.getBeansOfType(WriteAuthenticationTools.class).isEmpty(),
                "WriteAuthenticationTools should NOT be present in readonly mode");
        assertTrue(applicationContext.getBeansOfType(WriteAuthorizationTools.class).isEmpty(),
                "WriteAuthorizationTools should NOT be present in readonly mode");

        System.out.println("[DEBUG_LOG] Readonly interaction verified");
    }
}
