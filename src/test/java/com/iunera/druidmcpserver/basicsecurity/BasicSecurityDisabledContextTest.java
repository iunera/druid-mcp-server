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

import com.iunera.druidmcpserver.config.DruidProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verify that basic security beans are NOT loaded when the feature flag is disabled.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.extension.druid-basic-security.enabled=false",
        // even if readonly is false, feature flag should dominate
        "druid.mcp.readonly.enabled=false"
})
class BasicSecurityDisabledContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DruidProperties druidProperties;

    @Test
    void beansAbsentWhenDisabled() {
        System.out.println("[DEBUG_LOG] Checking absence of basic security beans when disabled");

        assertTrue(applicationContext.getBeansOfType(ReadAuthenticationTools.class).isEmpty(),
                "ReadAuthenticationTools should NOT be present when disabled");
        assertTrue(applicationContext.getBeansOfType(ReadAuthorizationTools.class).isEmpty(),
                "ReadAuthorizationTools should NOT be present when disabled");
        assertTrue(applicationContext.getBeansOfType(WriteAuthenticationTools.class).isEmpty(),
                "WriteAuthenticationTools should NOT be present when disabled");
        assertTrue(applicationContext.getBeansOfType(WriteAuthorizationTools.class).isEmpty(),
                "WriteAuthorizationTools should NOT be present when disabled");

        // also verify property binding reflects false
        assertFalse(druidProperties.getExtension().getDruidBasicSecurity().isEnabled(),
                "DruidProperties should bind enabled=false");

        System.out.println("[DEBUG_LOG] Basic security beans are absent and property binding verified");
    }
}
