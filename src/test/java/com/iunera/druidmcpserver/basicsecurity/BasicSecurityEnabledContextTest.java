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
 * Verify that basic security beans are loaded when the feature flag is enabled.
 */
@SpringBootTest
@TestPropertySource(properties = {
        // ensure feature is enabled
        "druid.extension.druid-basic-security.enabled=true",
        // ensure write tools are not blocked by readonly
        "druid.mcp.readonly.enabled=false"
})
class BasicSecurityEnabledContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DruidProperties druidProperties;

    @Test
    void beansPresentWhenEnabled() {
        System.out.println("[DEBUG_LOG] Checking presence of basic security beans when enabled");

        assertFalse(applicationContext.getBeansOfType(ReadAuthenticationTools.class).isEmpty(),
                "ReadAuthenticationTools should be present when enabled");
        assertFalse(applicationContext.getBeansOfType(ReadAuthorizationTools.class).isEmpty(),
                "ReadAuthorizationTools should be present when enabled");
        assertFalse(applicationContext.getBeansOfType(WriteAuthenticationTools.class).isEmpty(),
                "WriteAuthenticationTools should be present when enabled and not readonly");
        assertFalse(applicationContext.getBeansOfType(WriteAuthorizationTools.class).isEmpty(),
                "WriteAuthorizationTools should be present when enabled and not readonly");

        // also verify property binding
        assertTrue(druidProperties.getExtension().getDruidBasicSecurity().isEnabled(),
                "DruidProperties should bind enabled=true");

        System.out.println("[DEBUG_LOG] Basic security beans are present and property binding verified");
    }
}
