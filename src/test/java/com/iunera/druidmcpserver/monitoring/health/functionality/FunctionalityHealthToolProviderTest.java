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

package com.iunera.druidmcpserver.monitoring.health.functionality;

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
public class FunctionalityHealthToolProviderTest {

    @Autowired
    private FunctionalityHealthToolProvider functionalityHealthToolProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testFunctionalityHealthToolProviderExists() {
        System.out.println("[DEBUG_LOG] Testing FunctionalityHealthToolProvider exists");
        assertNotNull(functionalityHealthToolProvider, "FunctionalityHealthToolProvider should be autowired");
        System.out.println("[DEBUG_LOG] FunctionalityHealthToolProvider exists test passed");
    }

    @Test
    public void testCheckSupervisorHealth() {
        System.out.println("[DEBUG_LOG] Testing checkSupervisorHealth method");

        String result = functionalityHealthToolProvider.checkSupervisorHealth();
        assertNotNull(result, "checkSupervisorHealth should return a result");
        assertFalse(result.isEmpty(), "checkSupervisorHealth result should not be empty");

        System.out.println("[DEBUG_LOG] checkSupervisorHealth result: " + result);

        // Verify it's valid JSON
        try {
            JsonNode jsonResult = objectMapper.readTree(result);
            assertTrue(jsonResult.has("supervisor_count"), "Result should have supervisor_count");
            assertTrue(jsonResult.has("health_status"), "Result should have health_status");
            assertTrue(jsonResult.has("issues"), "Result should have issues");
            assertTrue(jsonResult.has("recommendations"), "Result should have recommendations");
            assertTrue(jsonResult.has("timestamp"), "Result should have timestamp");

            // Verify health status is one of expected values
            String healthStatus = jsonResult.get("health_status").asText();
            assertTrue(healthStatus.matches("HEALTHY|DEGRADED|CRITICAL|WARNING|ERROR"),
                    "Health status should be one of the expected values");

            System.out.println("[DEBUG_LOG] checkSupervisorHealth JSON structure is valid");
        } catch (Exception e) {
            fail("checkSupervisorHealth should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] checkSupervisorHealth test completed");
    }

    @Test
    public void testCheckHistoricalHealth() {
        System.out.println("[DEBUG_LOG] Testing checkHistoricalHealth method");

        String result = functionalityHealthToolProvider.checkHistoricalHealth();
        assertNotNull(result, "checkHistoricalHealth should return a result");
        assertFalse(result.isEmpty(), "checkHistoricalHealth result should not be empty");

        System.out.println("[DEBUG_LOG] checkHistoricalHealth result: " + result);

        // Verify it's valid JSON
        try {
            JsonNode jsonResult = objectMapper.readTree(result);
            assertTrue(jsonResult.has("total_segments"), "Result should have total_segments");
            assertTrue(jsonResult.has("health_status"), "Result should have health_status");
            assertTrue(jsonResult.has("issues"), "Result should have issues");
            assertTrue(jsonResult.has("recommendations"), "Result should have recommendations");
            assertTrue(jsonResult.has("timestamp"), "Result should have timestamp");

            // Verify health status is one of expected values
            String healthStatus = jsonResult.get("health_status").asText();
            assertTrue(healthStatus.matches("HEALTHY|DEGRADED|CRITICAL|WARNING|ERROR"),
                    "Health status should be one of the expected values");

            // Check for historical-specific fields
            assertTrue(jsonResult.has("segments_by_datasource"), "Result should have segments_by_datasource");
            assertTrue(jsonResult.has("segments_by_server"), "Result should have segments_by_server");

            System.out.println("[DEBUG_LOG] checkHistoricalHealth JSON structure is valid");
        } catch (Exception e) {
            fail("checkHistoricalHealth should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] checkHistoricalHealth test completed");
    }

    @Test
    public void testCheckFunctionalityHealth() {
        System.out.println("[DEBUG_LOG] Testing checkFunctionalityHealth method");

        String result = functionalityHealthToolProvider.checkFunctionalityHealth();
        assertNotNull(result, "checkFunctionalityHealth should return a result");
        assertFalse(result.isEmpty(), "checkFunctionalityHealth result should not be empty");

        System.out.println("[DEBUG_LOG] checkFunctionalityHealth result: " + result);

        // Verify it's valid JSON
        try {
            JsonNode jsonResult = objectMapper.readTree(result);
            assertTrue(jsonResult.has("supervisor_health"), "Result should have supervisor_health");
            assertTrue(jsonResult.has("historical_health"), "Result should have historical_health");
            assertTrue(jsonResult.has("overall_health_status"), "Result should have overall_health_status");
            assertTrue(jsonResult.has("overall_issues"), "Result should have overall_issues");
            assertTrue(jsonResult.has("overall_recommendations"), "Result should have overall_recommendations");
            assertTrue(jsonResult.has("task_summary"), "Result should have task_summary");
            assertTrue(jsonResult.has("timestamp"), "Result should have timestamp");

            // Verify overall health status is one of expected values
            String overallStatus = jsonResult.get("overall_health_status").asText();
            assertTrue(overallStatus.matches("HEALTHY|DEGRADED|CRITICAL|WARNING|ERROR"),
                    "Overall health status should be one of the expected values");

            // Verify task_summary contains expected fields
            JsonNode taskSummary = jsonResult.get("task_summary");
            assertTrue(taskSummary.has("running"), "Task summary should have running");
            assertTrue(taskSummary.has("pending"), "Task summary should have pending");
            assertTrue(taskSummary.has("complete"), "Task summary should have complete");

            System.out.println("[DEBUG_LOG] checkFunctionalityHealth JSON structure is valid");
        } catch (Exception e) {
            fail("checkFunctionalityHealth should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] checkFunctionalityHealth test completed");
    }

    @Test
    public void testQuickFunctionalityCheck() {
        System.out.println("[DEBUG_LOG] Testing quickFunctionalityCheck method");

        String result = functionalityHealthToolProvider.quickFunctionalityCheck();
        assertNotNull(result, "quickFunctionalityCheck should return a result");
        assertFalse(result.isEmpty(), "quickFunctionalityCheck result should not be empty");

        System.out.println("[DEBUG_LOG] quickFunctionalityCheck result: " + result);

        // Verify it's valid JSON
        try {
            JsonNode jsonResult = objectMapper.readTree(result);
            assertTrue(jsonResult.has("quick_health_status"), "Result should have quick_health_status");
            assertTrue(jsonResult.has("supervisor_count"), "Result should have supervisor_count");
            assertTrue(jsonResult.has("segment_count"), "Result should have segment_count");
            assertTrue(jsonResult.has("running_task_count"), "Result should have running_task_count");
            assertTrue(jsonResult.has("issues"), "Result should have issues");
            assertTrue(jsonResult.has("timestamp"), "Result should have timestamp");

            // Verify status is one of expected values
            String status = jsonResult.get("quick_health_status").asText();
            assertTrue(status.matches("HEALTHY|WARNING|CRITICAL|ERROR"),
                    "Status should be HEALTHY, WARNING, CRITICAL, or ERROR");

            // Verify counts are non-negative
            int supervisorCount = jsonResult.get("supervisor_count").asInt();
            int segmentCount = jsonResult.get("segment_count").asInt();
            int runningTaskCount = jsonResult.get("running_task_count").asInt();
            assertTrue(supervisorCount >= 0, "Supervisor count should be non-negative");
            assertTrue(segmentCount >= 0, "Segment count should be non-negative");
            assertTrue(runningTaskCount >= 0, "Running task count should be non-negative");

            System.out.println("[DEBUG_LOG] quickFunctionalityCheck JSON structure is valid");
        } catch (Exception e) {
            fail("quickFunctionalityCheck should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] quickFunctionalityCheck test completed");
    }

    @Test
    public void testFunctionalityHealthErrorHandling() {
        System.out.println("[DEBUG_LOG] Testing FunctionalityHealth error handling");

        // All methods should handle errors gracefully and return valid responses
        // even when Druid services are not available

        String supervisorResult = functionalityHealthToolProvider.checkSupervisorHealth();
        String historicalResult = functionalityHealthToolProvider.checkHistoricalHealth();
        String functionalityResult = functionalityHealthToolProvider.checkFunctionalityHealth();
        String quickCheckResult = functionalityHealthToolProvider.quickFunctionalityCheck();

        // All should return non-null, non-empty strings
        assertNotNull(supervisorResult);
        assertNotNull(historicalResult);
        assertNotNull(functionalityResult);
        assertNotNull(quickCheckResult);

        assertFalse(supervisorResult.isEmpty());
        assertFalse(historicalResult.isEmpty());
        assertFalse(functionalityResult.isEmpty());
        assertFalse(quickCheckResult.isEmpty());

        System.out.println("[DEBUG_LOG] All FunctionalityHealth methods handle errors gracefully");
        System.out.println("[DEBUG_LOG] FunctionalityHealth error handling test completed");
    }

    @Test
    public void testSupervisorHealthAnalysis() {
        System.out.println("[DEBUG_LOG] Testing supervisor health analysis features");

        String result = functionalityHealthToolProvider.checkSupervisorHealth();

        try {
            JsonNode jsonResult = objectMapper.readTree(result);

            // Check that supervisor health includes expected analysis
            assertTrue(jsonResult.has("supervisor_details"), "Should include supervisor_details");

            int supervisorCount = jsonResult.get("supervisor_count").asInt();
            assertTrue(supervisorCount >= 0, "Supervisor count should be non-negative");

            // If there are supervisors, check the details structure
            if (supervisorCount > 0) {
                JsonNode supervisorDetails = jsonResult.get("supervisor_details");
                assertTrue(supervisorDetails.isArray(), "supervisor_details should be an array");

                // Check first supervisor detail structure if available
                if (supervisorDetails.size() > 0) {
                    JsonNode firstSupervisor = supervisorDetails.get(0);
                    assertTrue(firstSupervisor.has("supervisor_id"), "Supervisor detail should have supervisor_id");
                    assertTrue(firstSupervisor.has("health"), "Supervisor detail should have health status");
                }
            }

            System.out.println("[DEBUG_LOG] Supervisor health analysis includes expected components");
            System.out.println("[DEBUG_LOG] Supervisor count: " + supervisorCount);

        } catch (Exception e) {
            fail("Supervisor health analysis should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] Supervisor health analysis test completed");
    }

    @Test
    public void testHistoricalHealthAnalysis() {
        System.out.println("[DEBUG_LOG] Testing historical health analysis features");

        String result = functionalityHealthToolProvider.checkHistoricalHealth();

        try {
            JsonNode jsonResult = objectMapper.readTree(result);

            // Check that historical health includes expected analysis
            assertTrue(jsonResult.has("total_segments"), "Should include total_segments");
            assertTrue(jsonResult.has("segments_by_datasource"), "Should include segments_by_datasource");
            assertTrue(jsonResult.has("segments_by_server"), "Should include segments_by_server");

            int totalSegments = jsonResult.get("total_segments").asInt();
            assertTrue(totalSegments >= 0, "Total segments should be non-negative");

            // Check segments by datasource structure
            JsonNode segmentsByDatasource = jsonResult.get("segments_by_datasource");
            assertTrue(segmentsByDatasource.isObject(), "segments_by_datasource should be an object");

            // Check segments by server structure
            JsonNode segmentsByServer = jsonResult.get("segments_by_server");
            assertTrue(segmentsByServer.isObject(), "segments_by_server should be an object");

            System.out.println("[DEBUG_LOG] Historical health analysis includes expected components");
            System.out.println("[DEBUG_LOG] Total segments: " + totalSegments);

        } catch (Exception e) {
            fail("Historical health analysis should return valid JSON: " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] Historical health analysis test completed");
    }
}
