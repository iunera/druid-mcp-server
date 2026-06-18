/*
 * Copyright (C) 2026 Christian Schmitt, Tim Frey
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
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.profiles.active=permissions",
        "druid.coordinator.url=http://localhost:8081"
})
class PermissionsProfileTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testPermissionsProfileTools() {
        assertTrue(applicationContext.containsBean("toolSpecs"), "toolSpecs bean should exist");
        Object toolSpecsObj = applicationContext.getBean("toolSpecs");
        assertTrue(toolSpecsObj instanceof java.util.List<?>, "toolSpecs should be a List");
        java.util.List<?> toolSpecs = (java.util.List<?>) toolSpecsObj;
        
        assertEquals(3, toolSpecs.size(), "Should have exactly 3 tools registered");
        
        java.util.List<String> expectedTools = Arrays.asList(
            "manageAuthentication", "manageAuthorization", "manageSecurityAssignments"
        );
        
        for (Object spec : toolSpecs) {
            String toolName = getToolName(spec);
            assertNotNull(toolName);
            assertTrue(expectedTools.contains(toolName), "Tool " + toolName + " should be in the whitelist");
        }
        
        // Assert that a query tool like "queryDruidSql" is not present
        boolean hasQueryTool = false;
        for (Object spec : toolSpecs) {
            if ("queryDruidSql".equals(getToolName(spec))) {
                hasQueryTool = true;
                break;
            }
        }
        assertFalse(hasQueryTool, "queryDruidSql should not be registered in permissions profile");
    }

    @Test
    void printAllRegisteredTools() throws Exception {
        // We can get the parent context or load a context without active profiles
        // to print all 93 tools. Or since user-management filters them, we can temporarily print them from here.
    }

    private String getToolName(Object spec) {
        try {
            Object tool = spec.getClass().getMethod("tool").invoke(spec);
            return (String) tool.getClass().getMethod("name").invoke(tool);
        } catch (Exception e) {
            return null;
        }
    }
}
