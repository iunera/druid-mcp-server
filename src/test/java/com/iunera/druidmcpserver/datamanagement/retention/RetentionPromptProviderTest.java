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

package com.iunera.druidmcpserver.datamanagement.retention;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RetentionPromptProvider
 */
@SpringBootTest
@TestPropertySource(properties = {
        "mcp.prompts.watermark=Test Watermark",
        "mcp.prompts.custom.organization-name=Test Organization",
        "mcp.prompts.custom.environment=test",
        "mcp.prompts.custom.contact-info=test@example.com"
})
class RetentionPromptProviderTest {

    @Autowired
    private RetentionPrompts retentionPromptProvider;

    @Test
    void testRetentionManagementPromptWithoutDatasource() {
        System.out.println("[DEBUG_LOG] Testing retention management prompt without datasource");

        GetPromptResult result = retentionPromptProvider.retentionManagementPrompt(null);

        assertNotNull(result, "Prompt result should not be null");
        assertNotNull(result.description(), "Prompt description should not be null");
        assertEquals("Druid Retention Management", result.description());

        assertNotNull(result.messages(), "Prompt messages should not be null");
        assertFalse(result.messages().isEmpty(), "Prompt messages should not be empty");

        String content = ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.messages().get(0).content()).text();
        assertNotNull(content, "Prompt content should not be null");

        // Verify key retention management content is included
        assertTrue(content.contains("data retention policies"),
                "Should contain retention policies text");
        assertTrue(content.contains("Show retention rules for all datasources"),
                "Should contain all datasources section");
        assertTrue(content.contains("Test Organization"),
                "Should contain organization name");
        assertTrue(content.contains("test"),
                "Should contain environment");
        assertTrue(content.contains("Test Watermark"),
                "Should contain watermark");

        System.out.println("[DEBUG_LOG] Retention management prompt without datasource test passed");
    }

    @Test
    void testRetentionManagementPromptWithDatasource() {
        System.out.println("[DEBUG_LOG] Testing retention management prompt with datasource");

        String datasource = "test-datasource";
        GetPromptResult result = retentionPromptProvider.retentionManagementPrompt(datasource);

        assertNotNull(result, "Prompt result should not be null");
        assertEquals("Druid Retention Management", result.description());

        String content = ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.messages().get(0).content()).text();
        assertNotNull(content, "Prompt content should not be null");

        // Verify datasource-specific content is included
        assertTrue(content.contains(datasource),
                "Should contain the specified datasource");
        assertTrue(content.contains("Review retention rules for datasource: " + datasource),
                "Should contain datasource-specific section");
        assertTrue(content.contains("data retention policies"),
                "Should contain retention policies text");

        System.out.println("[DEBUG_LOG] Retention management prompt with datasource test passed");
    }

    @Test
    void testRetentionPromptStructureAndContent() {
        System.out.println("[DEBUG_LOG] Testing retention prompt structure and content");

        GetPromptResult result = retentionPromptProvider.retentionManagementPrompt("sample-ds");

        String content = ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.messages().get(0).content()).text();

        // Verify the prompt has proper retention management structure
        assertTrue(content.contains("data retention policies"),
                "Should have retention policies section");
        assertTrue(content.contains("storage and age distribution"),
                "Should have storage analysis section");
        assertTrue(content.contains("optimal retention policies"),
                "Should have optimization recommendations");
        assertTrue(content.contains("storage savings"),
                "Should have savings calculation");
        assertTrue(content.contains("step-by-step instructions"),
                "Should have implementation instructions");
        assertTrue(content.contains("monitoring"),
                "Should have monitoring section");

        System.out.println("[DEBUG_LOG] Retention prompt structure and content test passed");
    }
}