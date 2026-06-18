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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
        "druid.coordinator.url=http://localhost:8081"
})
@ActiveProfiles("query-only")
class BasicSecurityReadonlyInteractionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testQueryOnlyProfileTools() {
        assertTrue(applicationContext.containsBean("toolSpecs"), "toolSpecs bean should exist");
        Object toolSpecsObj = applicationContext.getBean("toolSpecs");
        assertTrue(toolSpecsObj instanceof java.util.List<?>, "toolSpecs should be a List");
        java.util.List<?> toolSpecs = (java.util.List<?>) toolSpecsObj;
        assertEquals(11, toolSpecs.size(), "Should have exactly 11 tools registered");
        
        // Assert that a query tool like "getDatasources" is present
        boolean hasQueryTool = false;
        for (Object spec : toolSpecs) {
            if ("getDatasources".equals(getToolName(spec))) {
                hasQueryTool = true;
                break;
            }
        }
        assertTrue(hasQueryTool, "getDatasources should be registered in query-only profile");
        
        // Assert that a cluster admin tool like "manageDatasourceOrSegment" is not present
        boolean hasKillTool = false;
        for (Object spec : toolSpecs) {
            if ("manageDatasourceOrSegment".equals(getToolName(spec))) {
                hasKillTool = true;
                break;
            }
        }
        assertFalse(hasKillTool, "manageDatasourceOrSegment should not be registered in query-only profile");
        
        // Assert that a security tool like "manageAuthentication" is not present because query-only profile does not have it enabled
        boolean hasManageAuthTool = false;
        for (Object spec : toolSpecs) {
            if ("manageAuthentication".equals(getToolName(spec))) {
                hasManageAuthTool = true;
                break;
            }
        }
        assertFalse(hasManageAuthTool, "manageAuthentication should not be registered in query-only profile");
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
