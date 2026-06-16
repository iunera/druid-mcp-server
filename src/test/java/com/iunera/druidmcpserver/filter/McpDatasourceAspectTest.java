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

package com.iunera.druidmcpserver.filter;

import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(McpDatasourceAspectTest.TestToolComponent.class)
@TestPropertySource(properties = {
        "druid.mcp.tools.datasource-limits.wikipedia.enabled=testShowDatasourceDetails",
        "druid.mcp.tools.datasource-limits.restricted_ds.enabled=none"
})
class McpDatasourceAspectTest {

    @Autowired
    private TestToolComponent testComponent;

    @Autowired
    private McpToolProperties properties;

    @Test
    void testDatasourceLimitsConfigurationBinding() {
        assertNotNull(properties.getDatasourceLimits());
        var wikiLimits = properties.getDatasourceLimits().get("wikipedia");
        assertNotNull(wikiLimits);
        assertEquals(List.of("testShowDatasourceDetails"), wikiLimits.getEnabled());
    }

    @Test
    void testAspectAllowsPermittedTools() {
        // "testShowDatasourceDetails" is in enabled list for wikipedia
        String result = testComponent.showDatasourceDetails("wikipedia");
        assertEquals("ok-wikipedia", result);
    }

    @Test
    void testAspectBlocksNonEnabledTools() {
        // "testKillDatasource" is not in enabled list for wikipedia
        assertThrows(IllegalArgumentException.class, () -> {
            testComponent.killDatasource("wikipedia");
        });
    }

    @Test
    void testAspectAllowsAllForUnrestrictedDatasources() {
        // "other_ds" has no datasource limits defined
        String result = testComponent.killDatasource("other_ds");
        assertEquals("killed-other_ds", result);
    }

    @Component
    static class TestToolComponent {

        @McpTool(name = "testShowDatasourceDetails")
        public String showDatasourceDetails(String datasourceName) {
            return "ok-" + datasourceName;
        }

        @McpTool(name = "testKillDatasource")
        public String killDatasource(String datasourceName) {
            return "killed-" + datasourceName;
        }
    }
}
