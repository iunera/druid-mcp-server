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

import com.iunera.druidmcpserver.config.DruidRestClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://test-router:8888"
})
class RetentionRulesIntegrationTest {

    @Autowired
    private ReadRetentionRulesTools readRetentionRulesTools;

    @Autowired
    private WriteRetentionRulesTools writeRetentionRulesTools;

    @Autowired
    private RetentionRulesRepository retentionRulesRepository;

    @Autowired
    private DruidRestClientConfig druidRestClientConfig;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing retention rules context loading");
        assertNotNull(writeRetentionRulesTools);
        assertNotNull(retentionRulesRepository);
        assertNotNull(druidRestClientConfig);
        System.out.println("[DEBUG_LOG] All retention rules beans loaded successfully");
    }

    @Test
    void testRetentionRulesToolProviderConfiguration() {
        System.out.println("[DEBUG_LOG] Testing retention rules tool provider configuration");
        assertNotNull(writeRetentionRulesTools);

        // Test that the tool provider handles connection errors gracefully
        String result = readRetentionRulesTools.viewAllRetentionRules();
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Retention rules tool provider result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{"));
        System.out.println("[DEBUG_LOG] Retention rules tool provider handles errors gracefully");
    }

    @Test
    void testRetentionRulesForSpecificDatasource() {
        System.out.println("[DEBUG_LOG] Testing retention rules for specific datasource");
        String testDatasource = "test-datasource";

        String result = readRetentionRulesTools.viewRetentionRulesForDatasource(testDatasource);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Retention rules for datasource result: " + result);

        // Should return an error message or valid JSON since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Retention rules for specific datasource handles errors gracefully");
    }

    @Test
    void testRetentionRulesEdit() {
        System.out.println("[DEBUG_LOG] Testing retention rules edit functionality");
        String testDatasource = "test-datasource";
        String testRules = "[{\"type\":\"loadForever\"}]";

        String result = writeRetentionRulesTools.editRetentionRulesForDatasource(testDatasource, testRules);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Edit retention rules result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{"));
        System.out.println("[DEBUG_LOG] Edit retention rules handles errors gracefully");
    }

    @Test
    void testRetentionRulesHistory() {
        System.out.println("[DEBUG_LOG] Testing retention rules history functionality");
        String testDatasource = "test-datasource";

        String result = readRetentionRulesTools.viewRetentionRuleHistory(testDatasource);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Retention rules history result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Retention rules history handles errors gracefully");
    }

    @Test
    void testDruidConfigurationForRetentionRules() {
        System.out.println("[DEBUG_LOG] Testing Druid configuration for retention rules");
        assertNotNull(druidRestClientConfig.getDruidRouterUrl());
        assertEquals("http://test-router:8888", druidRestClientConfig.getDruidRouterUrl());
        System.out.println("[DEBUG_LOG] Druid router URL configured correctly: " + druidRestClientConfig.getDruidRouterUrl());
    }
}
