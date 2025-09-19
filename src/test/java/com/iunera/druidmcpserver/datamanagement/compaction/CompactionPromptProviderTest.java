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

package com.iunera.druidmcpserver.datamanagement.compaction;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CompactionPromptProvider
 */
@SpringBootTest
@TestPropertySource(properties = {
        "mcp.prompts.watermark=Test Watermark",
        "mcp.prompts.custom.organization-name=Test Organization",
        "mcp.prompts.custom.environment=test",
        "mcp.prompts.custom.contact-info=test@example.com"
})
class CompactionPromptProviderTest {

    @Autowired
    private CompactionPrompts compactionPromptProvider;

    @Test
    void testCompactionSuggestionsPromptWithoutParameters() {
        System.out.println("[DEBUG_LOG] Testing compaction suggestions prompt without parameters");

        GetPromptResult result = compactionPromptProvider.compactionSuggestionsPrompt(null, null, null);

        assertNotNull(result, "Prompt result should not be null");
        assertNotNull(result.description(), "Prompt description should not be null");
        assertEquals("Apache Druid Compaction Suggestions", result.description());

        assertNotNull(result.messages(), "Prompt messages should not be null");
        assertFalse(result.messages().isEmpty(), "Prompt messages should not be empty");

        String content = ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.messages().get(0).content()).text();
        assertNotNull(content, "Prompt content should not be null");

        // Verify key Apache Druid recommendations are included
        assertTrue(content.contains("Apache Druid Compaction Configuration Suggestions"),
                "Should contain main title");
        assertTrue(content.contains("300-700MB"),
                "Should contain segment size recommendations");
        assertTrue(content.contains("inputSegmentSizeBytes"),
                "Should contain configuration parameters");
        assertTrue(content.contains("maxRowsPerSegment"),
                "Should contain row count configuration");
        assertTrue(content.contains("skipOffsetFromLatest"),
                "Should contain offset configuration");
        assertTrue(content.contains("taskPriority"),
                "Should contain priority configuration");

        System.out.println("[DEBUG_LOG] Compaction suggestions prompt test passed");
    }

    @Test
    void testCompactionSuggestionsPromptWithParameters() {
        System.out.println("[DEBUG_LOG] Testing compaction suggestions prompt with parameters");

        String datasource = "test-datasource";
        String queryPatterns = "time-series aggregations";
        String dataVolume = "high";

        GetPromptResult result = compactionPromptProvider.compactionSuggestionsPrompt(
                datasource, queryPatterns, dataVolume);

        assertNotNull(result, "Prompt result should not be null");

        String content = ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.messages().get(0).content()).text();
        assertNotNull(content, "Prompt content should not be null");

        // Verify parameters are included in the prompt
        assertTrue(content.contains(datasource),
                "Should contain the specified datasource");
        assertTrue(content.contains(queryPatterns),
                "Should contain the specified query patterns");
        assertTrue(content.contains(dataVolume),
                "Should contain the specified data volume");

        System.out.println("[DEBUG_LOG] Compaction suggestions prompt with parameters test passed");
    }

    @Test
    void testCompactionTroubleshootingPrompt() {
        System.out.println("[DEBUG_LOG] Testing compaction troubleshooting prompt");

        String issueDescription = "Compaction tasks are failing with OOM errors";
        String datasource = "problematic-datasource";

        GetPromptResult result = compactionPromptProvider.compactionTroubleshootingPrompt(
                issueDescription, datasource);

        assertNotNull(result, "Prompt result should not be null");
        assertEquals("Druid Compaction Troubleshooting", result.description());

        String content = ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.messages().get(0).content()).text();
        assertNotNull(content, "Prompt content should not be null");

        // Verify troubleshooting content is included
        assertTrue(content.contains("Druid Compaction Troubleshooting"),
                "Should contain troubleshooting title");
        assertTrue(content.contains("out-of-memory errors"),
                "Should contain OOM troubleshooting");
        assertTrue(content.contains("Resource Optimization"),
                "Should contain resource optimization section");
        assertTrue(content.contains("heap size"),
                "Should contain heap size recommendations");
        assertTrue(content.contains(issueDescription),
                "Should contain the issue description");
        assertTrue(content.contains(datasource),
                "Should contain the datasource name");

        System.out.println("[DEBUG_LOG] Compaction troubleshooting prompt test passed");
    }

    @Test
    void testPromptStructureAndContent() {
        System.out.println("[DEBUG_LOG] Testing prompt structure and content quality");

        GetPromptResult result = compactionPromptProvider.compactionSuggestionsPrompt(
                "test-ds", "groupBy queries", "medium");

        String content = ((io.modelcontextprotocol.spec.McpSchema.TextContent) result.messages().get(0).content()).text();

        // Verify the prompt has proper structure
        assertTrue(content.contains("## Current Context:"),
                "Should have context section");
        assertTrue(content.contains("### 1. Current State Assessment"),
                "Should have assessment section");
        assertTrue(content.contains("### 2. Apache Druid Best Practice Recommendations"),
                "Should have best practices section");
        assertTrue(content.contains("### 3. Configuration Parameters Analysis"),
                "Should have configuration section");
        assertTrue(content.contains("### 4. Performance Impact Estimation"),
                "Should have performance section");
        assertTrue(content.contains("### 5. Implementation Strategy"),
                "Should have implementation section");
        assertTrue(content.contains("### 6. Monitoring and Maintenance"),
                "Should have monitoring section");

        // Verify Apache Druid specific recommendations
        assertTrue(content.contains("5-10 million rows per segment"),
                "Should contain row count recommendations");
        assertTrue(content.contains("hash vs range partitioning"),
                "Should contain partitioning strategies");
        assertTrue(content.contains("maxNumConcurrentSubTasks"),
                "Should contain tuning parameters");

        System.out.println("[DEBUG_LOG] Prompt structure and content test passed");
    }
}
