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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verify that basic security beans are loaded when the user-management profile is active and coordinator url is set.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.profiles.active=user-management",
        "druid.coordinator.url=http://localhost:8081"
})
class BasicSecurityEnabledContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DruidProperties druidProperties;

    @Test
    void beansPresentWhenEnabled() {
        System.out.println("[DEBUG_LOG] Checking presence of basic security beans when enabled");

        assertFalse(applicationContext.getBeansOfType(SecurityTools.class).isEmpty(),
                "SecurityTools should be present when user-management profile is active and coordinator url is set");

        assertEquals("http://localhost:8081", druidProperties.getCoordinator().getUrl(),
                "DruidProperties coordinator url should be set");

        System.out.println("[DEBUG_LOG] Basic security beans are present verified");
    }
}
