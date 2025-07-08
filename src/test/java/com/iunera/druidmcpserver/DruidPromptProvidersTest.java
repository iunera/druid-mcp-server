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

package com.iunera.druidmcpserver;

import com.iunera.druidmcpserver.datamanagement.query.DataAnalysisPromptProvider;
import com.iunera.druidmcpserver.datamanagement.retention.RetentionPromptProvider;
import com.iunera.druidmcpserver.ingestion.IngestionManagementPromptProvider;
import com.iunera.druidmcpserver.monitoring.health.prompts.ClusterManagementPromptProvider;
import com.iunera.druidmcpserver.operations.OperationalPromptProvider;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Druid MCP Prompt Providers
 */
@SpringBootTest
@TestPropertySource(properties = {
        "mcp.prompts.watermark=Test Watermark - Druid MCP Server",
        "mcp.prompts.custom.organization-name=Test Organization",
        "mcp.prompts.custom.environment=test",
        "mcp.prompts.custom.contact-info=test@example.com"
})
class DruidPromptProvidersTest {

    @Autowired
    private DataAnalysisPromptProvider dataAnalysisPromptProvider;

    @Autowired
    private ClusterManagementPromptProvider clusterManagementPromptProvider;

    @Autowired
    private IngestionManagementPromptProvider ingestionManagementPromptProvider;

    @Autowired
    private RetentionPromptProvider retentionPromptProvider;

    @Autowired
    private OperationalPromptProvider operationalPromptProvider;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing context loading for prompt providers");
        assertNotNull(dataAnalysisPromptProvider);
        assertNotNull(clusterManagementPromptProvider);
        assertNotNull(ingestionManagementPromptProvider);
        assertNotNull(retentionPromptProvider);
        assertNotNull(operationalPromptProvider);
        System.out.println("[DEBUG_LOG] All prompt providers loaded successfully");
    }

    @Test
    void testDataExplorationPrompt() {
        System.out.println("[DEBUG_LOG] Testing data exploration prompt");

        GetPromptResult result = dataAnalysisPromptProvider.dataExplorationPrompt("test-datasource");

        assertNotNull(result);
        assertEquals("Druid Data Exploration", result.description());
        assertNotNull(result.messages());
        assertFalse(result.messages().isEmpty());

        // Access the text content properly
        TextContent textContent = (TextContent) result.messages().get(0).content();
        String content = textContent.text();
        assertTrue(content.contains("test-datasource"));
        assertTrue(content.contains("Test Organization"));
        assertTrue(content.contains("test"));
        assertTrue(content.contains("Test Watermark"));

        System.out.println("[DEBUG_LOG] Data exploration prompt test passed");
    }

    @Test
    void testQueryOptimizationPrompt() {
        System.out.println("[DEBUG_LOG] Testing query optimization prompt");

        String testQuery = "SELECT * FROM test_datasource WHERE __time >= CURRENT_TIMESTAMP - INTERVAL '1' DAY";
        GetPromptResult result = dataAnalysisPromptProvider.queryOptimizationPrompt(testQuery);

        assertNotNull(result);
        assertEquals("Druid Query Optimization", result.description());
        assertNotNull(result.messages());
        assertFalse(result.messages().isEmpty());

        TextContent textContent = (TextContent) result.messages().get(0).content();
        String content = textContent.text();
        assertTrue(content.contains(testQuery));
        assertTrue(content.contains("Test Organization"));
        assertTrue(content.contains("test@example.com"));

        System.out.println("[DEBUG_LOG] Query optimization prompt test passed");
    }

    @Test
    void testHealthCheckPrompt() {
        System.out.println("[DEBUG_LOG] Testing health check prompt");

        GetPromptResult result = clusterManagementPromptProvider.healthCheckPrompt();

        assertNotNull(result);
        assertEquals("Druid Health Check", result.description());
        assertNotNull(result.messages());
        assertFalse(result.messages().isEmpty());

        TextContent textContent = (TextContent) result.messages().get(0).content();
        String content = textContent.text();
        assertTrue(content.contains("comprehensive health check"));
        assertTrue(content.contains("Test Organization"));
        assertTrue(content.contains("test"));

        System.out.println("[DEBUG_LOG] Health check prompt test passed");
    }

    @Test
    void testEmergencyResponsePrompt() {
        System.out.println("[DEBUG_LOG] Testing emergency response prompt");

        GetPromptResult result = operationalPromptProvider.emergencyResponsePrompt();

        assertNotNull(result);
        assertEquals("Druid Emergency Response", result.description());
        assertNotNull(result.messages());
        assertFalse(result.messages().isEmpty());

        TextContent textContent = (TextContent) result.messages().get(0).content();
        String content = textContent.text();
        assertTrue(content.contains("urgent issue"));
        assertTrue(content.contains("**URGENT"));
        assertTrue(content.contains("Emergency Contact"));

        System.out.println("[DEBUG_LOG] Emergency response prompt test passed");
    }

    @Test
    void testWatermarkConfiguration() {
        System.out.println("[DEBUG_LOG] Testing watermark configuration");

        GetPromptResult result = dataAnalysisPromptProvider.dataExplorationPrompt(null);
        TextContent textContent = (TextContent) result.messages().get(0).content();
        String content = textContent.text();

        assertTrue(content.contains("Test Watermark - Druid MCP Server"));

        System.out.println("[DEBUG_LOG] Watermark configuration test passed");
    }
}