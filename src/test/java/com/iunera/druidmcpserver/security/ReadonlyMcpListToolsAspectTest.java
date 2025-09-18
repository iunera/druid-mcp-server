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

package com.iunera.druidmcpserver.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests filtering of MCP tools listing via ReadonlyMcpListToolsAspect.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.mcp.readonly.enabled=true"
})
@Import(ReadonlyMcpListToolsAspectTest.FakeMcpServer.class)
class ReadonlyMcpListToolsAspectTest {

    @Autowired
    FakeMcpServer server;

    @Test
    void listTools_isFilteredInReadonlyMode() {
        FakeListToolsResult res = server.listTools();
        assertNotNull(res);
        List<String> names = res.tools().stream().map(FakeTool::name).collect(Collectors.toList());
        System.out.println("[DEBUG_LOG] Filtered tool names = " + names);
        // Allowed: list*, queryDruidSql
        assertTrue(names.contains("listDatasources"));
        assertTrue(names.contains("queryDruidSql"));
        // Disallowed examples
        assertFalse(names.contains("cancelMultiStageQueryTask"));
        assertFalse(names.contains("editCompactionConfigForDatasource"));
        // Ensure only the allowed tools remain
        assertEquals(2, names.size());
    }

    @Component
    static class FakeMcpServer {
        // Method name matches aspect pointcut execution(* *..*listTools(..))
        public FakeListToolsResult listTools() {
            List<FakeTool> tools = new ArrayList<>(Arrays.asList(
                    new FakeTool("listDatasources", "List datasources"),
                    new FakeTool("editCompactionConfigForDatasource", "Edit compaction"),
                    new FakeTool("queryDruidSql", "Execute SQL"),
                    new FakeTool("cancelMultiStageQueryTask", "Cancel task")
            ));
            return new FakeListToolsResult(tools);
        }
    }

    // Class name contains "ListToolsResult" to trigger aspect
    static class FakeListToolsResult {
        private final List<FakeTool> tools;
        public FakeListToolsResult(List<FakeTool> tools) {
            this.tools = tools;
        }
        public List<FakeTool> tools() { return tools; }
        // Optional nextCursor() method; not used but present to match reflection path
        public String nextCursor() { return null; }
    }

    static class FakeTool {
        private final String name;
        private final String description;
        FakeTool(String name, String description) {
            this.name = name;
            this.description = description;
        }
        public String name() { return name; }
        public String description() { return description; }
    }
}
