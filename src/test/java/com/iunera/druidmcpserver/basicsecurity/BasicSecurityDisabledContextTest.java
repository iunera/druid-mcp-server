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
 * Verify that basic security beans are NOT loaded when either condition is missing (e.g. active profile is not user-management, or coordinator url is empty).
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.profiles.active=query-only",
        "druid.coordinator.url="
})
class BasicSecurityDisabledContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DruidProperties druidProperties;

    @Test
    void beansAbsentWhenDisabled() {
        System.out.println("[DEBUG_LOG] Checking absence of basic security beans when disabled");

        assertTrue(applicationContext.getBeansOfType(SecurityTools.class).isEmpty(),
                "SecurityTools should NOT be present when user-management profile is inactive and coordinator url is empty");

        assertTrue(druidProperties.getCoordinator().getUrl().isEmpty(),
                "DruidProperties coordinator url should be empty");

        System.out.println("[DEBUG_LOG] Basic security beans are absent verified");
    }
}
