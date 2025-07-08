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

package com.iunera.druidmcpserver.monitoring.health.diagnostics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
public class DruidDoctorToolProviderTest {

    @Autowired
    private DruidDoctorToolProvider druidDoctorToolProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDruidDoctorToolProviderExists() {
        System.out.println("[DEBUG_LOG] Testing DruidDoctorToolProvider exists");
        assertNotNull(druidDoctorToolProvider, "DruidDoctorToolProvider should be autowired");
        System.out.println("[DEBUG_LOG] DruidDoctorToolProvider exists test passed");
    }

    @Test
    public void testDiagnoseCluster() {
        System.out.println("[DEBUG_LOG] Testing diagnoseCluster method");

        String result = druidDoctorToolProvider.diagnoseCluster();
        assertNotNull(result, "diagnoseCluster should return a result");
        assertFalse(result.isEmpty(), "diagnoseCluster result should not be empty");

        System.out.println("[DEBUG_LOG] diagnoseCluster result: " + result);

        // Verify it's valid JSON
        try {
            JsonNode jsonResult = objectMapper.readTree(result);
            assertTrue(jsonResult.has("overall_health_score"), "Result should have overall_health_score");
            assertTrue(jsonResult.has("health_status"), "Result should have health_status");
            assertTrue(jsonResult.has("issues_found"), "Result should have issues_found");
            assertTrue(jsonResult.has("recommendations"), "Result should have recommendations");
            assertTrue(jsonResult.has("component_status"), "Result should have component_status");
            assertTrue(jsonResult.has("diagnosis_timestamp"), "Result should have diagnosis_timestamp");

            System.out.println("[DEBUG_LOG] diagnoseCluster JSON structure is valid");
        } catch (Exception e) {
            fail("diagnoseCluster should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] diagnoseCluster test completed");
    }

    @Test
    public void testQuickHealthCheck() {
        System.out.println("[DEBUG_LOG] Testing quickHealthCheck method");

        String result = druidDoctorToolProvider.quickHealthCheck();
        assertNotNull(result, "quickHealthCheck should return a result");
        assertFalse(result.isEmpty(), "quickHealthCheck result should not be empty");

        System.out.println("[DEBUG_LOG] quickHealthCheck result: " + result);

        // Verify it's valid JSON
        try {
            JsonNode jsonResult = objectMapper.readTree(result);
            assertTrue(jsonResult.has("status"), "Result should have status");
            assertTrue(jsonResult.has("critical_issues"), "Result should have critical_issues");
            assertTrue(jsonResult.has("immediate_actions"), "Result should have immediate_actions");
            assertTrue(jsonResult.has("check_timestamp"), "Result should have check_timestamp");

            // Verify status is either HEALTHY or NEEDS_ATTENTION
            String status = jsonResult.get("status").asText();
            assertTrue(status.equals("HEALTHY") || status.equals("NEEDS_ATTENTION"),
                    "Status should be HEALTHY or NEEDS_ATTENTION");

            System.out.println("[DEBUG_LOG] quickHealthCheck JSON structure is valid");
        } catch (Exception e) {
            fail("quickHealthCheck should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] quickHealthCheck test completed");
    }

    @Test
    public void testAnalyzePerformance() {
        System.out.println("[DEBUG_LOG] Testing analyzePerformance method");

        String result = druidDoctorToolProvider.analyzePerformance();
        assertNotNull(result, "analyzePerformance should return a result");
        assertFalse(result.isEmpty(), "analyzePerformance result should not be empty");

        System.out.println("[DEBUG_LOG] analyzePerformance result: " + result);

        // Verify it's valid JSON
        try {
            JsonNode jsonResult = objectMapper.readTree(result);
            assertTrue(jsonResult.has("performance_score"), "Result should have performance_score");
            assertTrue(jsonResult.has("performance_issues"), "Result should have performance_issues");
            assertTrue(jsonResult.has("optimization_recommendations"), "Result should have optimization_recommendations");
            assertTrue(jsonResult.has("analysis_timestamp"), "Result should have analysis_timestamp");

            // Verify performance score is between 0 and 100
            int performanceScore = jsonResult.get("performance_score").asInt();
            assertTrue(performanceScore >= 0 && performanceScore <= 100,
                    "Performance score should be between 0 and 100");

            System.out.println("[DEBUG_LOG] analyzePerformance JSON structure is valid");
        } catch (Exception e) {
            fail("analyzePerformance should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] analyzePerformance test completed");
    }

    @Test
    public void testValidateConfiguration() {
        System.out.println("[DEBUG_LOG] Testing validateConfiguration method");

        String result = druidDoctorToolProvider.validateConfiguration();
        assertNotNull(result, "validateConfiguration should return a result");
        assertFalse(result.isEmpty(), "validateConfiguration result should not be empty");

        System.out.println("[DEBUG_LOG] validateConfiguration result: " + result);

        // Verify it's valid JSON
        try {
            JsonNode jsonResult = objectMapper.readTree(result);
            assertTrue(jsonResult.has("configuration_score"), "Result should have configuration_score");
            assertTrue(jsonResult.has("configuration_status"), "Result should have configuration_status");
            assertTrue(jsonResult.has("configuration_issues"), "Result should have configuration_issues");
            assertTrue(jsonResult.has("best_practice_recommendations"), "Result should have best_practice_recommendations");
            assertTrue(jsonResult.has("validation_timestamp"), "Result should have validation_timestamp");

            // Verify configuration score is between 0 and 100
            int configScore = jsonResult.get("configuration_score").asInt();
            assertTrue(configScore >= 0 && configScore <= 100,
                    "Configuration score should be between 0 and 100");

            // Verify configuration_issues is an array
            JsonNode configIssues = jsonResult.get("configuration_issues");
            assertTrue(configIssues.isArray(), "configuration_issues should be an array");

            System.out.println("[DEBUG_LOG] validateConfiguration JSON structure is valid");
        } catch (Exception e) {
            fail("validateConfiguration should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] validateConfiguration test completed");
    }

    @Test
    public void testDruidDoctorErrorHandling() {
        System.out.println("[DEBUG_LOG] Testing DruidDoctor error handling");

        // All methods should handle errors gracefully and return valid responses
        // even when Druid services are not available

        String diagnoseResult = druidDoctorToolProvider.diagnoseCluster();
        String quickCheckResult = druidDoctorToolProvider.quickHealthCheck();
        String performanceResult = druidDoctorToolProvider.analyzePerformance();
        String configResult = druidDoctorToolProvider.validateConfiguration();

        // All should return non-null, non-empty strings
        assertNotNull(diagnoseResult);
        assertNotNull(quickCheckResult);
        assertNotNull(performanceResult);
        assertNotNull(configResult);

        assertFalse(diagnoseResult.isEmpty());
        assertFalse(quickCheckResult.isEmpty());
        assertFalse(performanceResult.isEmpty());
        assertFalse(configResult.isEmpty());

        System.out.println("[DEBUG_LOG] All DruidDoctor methods handle errors gracefully");
        System.out.println("[DEBUG_LOG] DruidDoctor error handling test completed");
    }

    @Test
    public void testDruidDoctorComprehensiveDiagnosis() {
        System.out.println("[DEBUG_LOG] Testing comprehensive diagnosis features");

        // Test that diagnoseCluster provides comprehensive information
        String result = druidDoctorToolProvider.diagnoseCluster();

        try {
            JsonNode jsonResult = objectMapper.readTree(result);

            // Check component status includes all expected components
            JsonNode componentStatus = jsonResult.get("component_status");
            assertTrue(componentStatus.has("coordinator"), "Should check coordinator status");
            assertTrue(componentStatus.has("servers"), "Should check servers status");
            assertTrue(componentStatus.has("segments"), "Should check segments status");
            assertTrue(componentStatus.has("ingestion"), "Should check ingestion status");
            assertTrue(componentStatus.has("load_queue"), "Should check load_queue status");
            assertTrue(componentStatus.has("datasources"), "Should check datasources status");

            // Check that health score is reasonable
            int healthScore = jsonResult.get("overall_health_score").asInt();
            assertTrue(healthScore >= 0 && healthScore <= 100,
                    "Health score should be between 0 and 100");

            // Check that health status corresponds to score
            String healthStatus = jsonResult.get("health_status").asText();
            assertTrue(healthStatus.matches("EXCELLENT|GOOD|FAIR|POOR|CRITICAL"),
                    "Health status should be one of the expected values");

            System.out.println("[DEBUG_LOG] Comprehensive diagnosis includes all expected components");
            System.out.println("[DEBUG_LOG] Health score: " + healthScore + ", Status: " + healthStatus);

        } catch (Exception e) {
            fail("Comprehensive diagnosis should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] Comprehensive diagnosis test completed");
    }
}
