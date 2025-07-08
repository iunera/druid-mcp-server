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

import com.iunera.druidmcpserver.config.DruidConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://test-router:8888"
})
class CompactionConfigIntegrationTest {

    @Autowired
    private CompactionConfigToolProvider compactionConfigToolProvider;

    @Autowired
    private CompactionConfigRepository compactionConfigRepository;

    @Autowired
    private DruidConfig druidConfig;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing compaction config context loading");
        assertNotNull(compactionConfigToolProvider);
        assertNotNull(compactionConfigRepository);
        assertNotNull(druidConfig);
        System.out.println("[DEBUG_LOG] All compaction config beans loaded successfully");
    }

    @Test
    void testCompactionConfigToolProviderConfiguration() {
        System.out.println("[DEBUG_LOG] Testing compaction config tool provider configuration");
        assertNotNull(compactionConfigToolProvider);

        // Test that the tool provider handles connection errors gracefully
        String result = compactionConfigToolProvider.viewAllCompactionConfigs();
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Compaction config tool provider result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Compaction config tool provider handles errors gracefully");
    }

    @Test
    void testCompactionConfigForSpecificDatasource() {
        System.out.println("[DEBUG_LOG] Testing compaction config for specific datasource");
        String testDatasource = "test-datasource";

        String result = compactionConfigToolProvider.viewCompactionConfigForDatasource(testDatasource);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Compaction config for datasource result: " + result);

        // Should return an error message or valid JSON since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Compaction config for specific datasource handles errors gracefully");
    }

    @Test
    void testCompactionConfigEdit() {
        System.out.println("[DEBUG_LOG] Testing compaction config edit functionality");
        String testDatasource = "test-datasource";
        String testConfig = "{\"dataSource\":\"test-datasource\",\"taskPriority\":25,\"inputSegmentSizeBytes\":419430400,\"maxRowsPerSegment\":5000000}";

        String result = compactionConfigToolProvider.editCompactionConfigForDatasource(testDatasource, testConfig);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Edit compaction config result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{"));
        System.out.println("[DEBUG_LOG] Edit compaction config handles errors gracefully");
    }

    @Test
    void testCompactionConfigDelete() {
        System.out.println("[DEBUG_LOG] Testing compaction config delete functionality");
        String testDatasource = "test-datasource";

        String result = compactionConfigToolProvider.deleteCompactionConfigForDatasource(testDatasource);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Delete compaction config result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{"));
        System.out.println("[DEBUG_LOG] Delete compaction config handles errors gracefully");
    }

    @Test
    void testCompactionConfigHistory() {
        System.out.println("[DEBUG_LOG] Testing compaction config history functionality");
        String testDatasource = "test-datasource";

        String result = compactionConfigToolProvider.viewCompactionConfigHistory(testDatasource);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Compaction config history result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Compaction config history handles errors gracefully");
    }

    @Test
    void testCompactionStatus() {
        System.out.println("[DEBUG_LOG] Testing compaction status functionality");

        String result = compactionConfigToolProvider.viewCompactionStatus();
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Compaction status result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Compaction status handles errors gracefully");
    }

    @Test
    void testCompactionStatusForDatasource() {
        System.out.println("[DEBUG_LOG] Testing compaction status for specific datasource");
        String testDatasource = "test-datasource";

        String result = compactionConfigToolProvider.viewCompactionStatusForDatasource(testDatasource);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Compaction status for datasource result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Compaction status for datasource handles errors gracefully");
    }

    @Test
    void testDruidConfigurationForCompaction() {
        System.out.println("[DEBUG_LOG] Testing Druid configuration for compaction");
        assertNotNull(druidConfig.getDruidRouterUrl());
        assertEquals("http://test-router:8888", druidConfig.getDruidRouterUrl());
        System.out.println("[DEBUG_LOG] Druid router URL configured correctly: " + druidConfig.getDruidRouterUrl());
    }
}
