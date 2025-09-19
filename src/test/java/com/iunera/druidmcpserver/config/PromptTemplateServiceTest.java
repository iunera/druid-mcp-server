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

package com.iunera.druidmcpserver.config;

import com.iunera.druidmcpserver.datamanagement.query.DataAnalysisPrompts;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for PromptTemplateService to verify property override functionality
 */
@SpringBootTest
@TestPropertySource(properties = {
        "mcp.prompts.watermark=Custom Test Watermark Override",
        "mcp.prompts.custom.organization-name=Override Organization",
        "mcp.prompts.custom.environment=override-env",
        "mcp.prompts.custom.contact-info=override@example.com",
        "prompts.druid-data-exploration.template=CUSTOM TEMPLATE: This is a custom overridden template for testing. Environment: {environment}, Organization: {organizationName}, Watermark: {watermark}"
})
class PromptTemplateServiceTest {

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private DataAnalysisPrompts dataAnalysisPromptProvider;

    @Test
    void testPropertyOverrides() {
        System.out.println("[DEBUG_LOG] Testing property override functionality");

        // Test that custom properties are loaded correctly
        GetPromptResult result = dataAnalysisPromptProvider.dataExplorationPrompt(null);

        assertNotNull(result, "Prompt result should not be null");

        String content = ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.messages().get(0).content()).text();
        assertNotNull(content, "Prompt content should not be null");

        // Verify that overridden values are used
        assertTrue(content.contains("CUSTOM TEMPLATE"),
                "Should contain custom template text");
        assertTrue(content.contains("Custom Test Watermark Override"),
                "Should contain overridden watermark");
        assertTrue(content.contains("Override Organization"),
                "Should contain overridden organization name");
        assertTrue(content.contains("override-env"),
                "Should contain overridden environment");

        System.out.println("[DEBUG_LOG] Property override test passed");
        System.out.println("[DEBUG_LOG] Content: " + content);
    }

    @Test
    void testTemplateVariableSubstitution() {
        System.out.println("[DEBUG_LOG] Testing template variable substitution");

        // Test the template service directly
        java.util.Map<String, String> variables = promptTemplateService.createVariables();
        promptTemplateService.addVariable(variables, "testVar", "testValue");

        String template = promptTemplateService.loadTemplate("prompts.druid-data-exploration.template", variables);

        assertNotNull(template, "Template should not be null");
        assertTrue(template.contains("CUSTOM TEMPLATE"),
                "Should contain custom template text");
        assertTrue(template.contains("Custom Test Watermark Override"),
                "Should contain watermark substitution");
        assertTrue(template.contains("Override Organization"),
                "Should contain organization substitution");

        System.out.println("[DEBUG_LOG] Template variable substitution test passed");
    }

    @Test
    void testSectionLoading() {
        System.out.println("[DEBUG_LOG] Testing section loading functionality");

        java.util.Map<String, String> variables = promptTemplateService.createVariables();
        promptTemplateService.addVariable(variables, "datasource", "test-datasource");

        String section = promptTemplateService.loadSection("prompts.druid-data-exploration.datasource-section", variables);

        assertNotNull(section, "Section should not be null");
        assertTrue(section.contains("test-datasource"),
                "Section should contain substituted datasource");

        System.out.println("[DEBUG_LOG] Section loading test passed");
        System.out.println("[DEBUG_LOG] Section content: " + section);
    }
}