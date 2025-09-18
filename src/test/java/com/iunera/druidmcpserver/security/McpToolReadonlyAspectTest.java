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
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "druid.mcp.readonly.enabled=true"
})
@Import(McpToolReadonlyAspectTest.TestTools.class)
class McpToolReadonlyAspectTest {

    @Autowired
    private TestTools tools;

    @Component
    static class TestTools {
        @McpTool(description = "read-only style tool")
        public String getSomething(String id) {
            return "got:" + id;
        }
        @McpTool(description = "mutating tool")
        public String killSomething(String id) {
            return "killed:" + id;
        }
    }

    @Test
    void readonlyAllowsGetStyleTool() {
        String res = tools.getSomething("42");
        System.out.println("[DEBUG_LOG] getSomething => " + res);
        assertEquals("got:42", res);
    }

    @Test
    void readonlyBlocksMutatingTool() {
        String res = tools.killSomething("42");
        System.out.println("[DEBUG_LOG] killSomething => " + res);
        assertTrue(res.contains("read_only_mode"), "Expected JSON error for blocked tool");
    }
}
