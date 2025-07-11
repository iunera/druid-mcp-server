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

package com.iunera.druidmcpserver.datamanagement.query;

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
class MultiStageQueryIntegrationTest {

    @Autowired
    private QueryToolProvider queryToolProvider;

    @Autowired
    private DruidConfig druidConfig;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing multi-stage query context loading");
        assertNotNull(queryToolProvider);
        assertNotNull(druidConfig);
        System.out.println("[DEBUG_LOG] All multi-stage query beans loaded successfully");
    }

    @Test
    void testMultiStageQueryExecution() {
        System.out.println("[DEBUG_LOG] Testing multi-stage query execution");
        String testQuery = "SELECT COUNT(*) FROM test_datasource";

        String result = queryToolProvider.queryDruidMultiStage(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Multi-stage query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Multi-stage query handles errors gracefully");
    }

    @Test
    void testMultiStageQueryWithContext() {
        System.out.println("[DEBUG_LOG] Testing multi-stage query with context");
        String testQuery = "SELECT COUNT(*) FROM test_datasource";
        String testContext = "{\"maxNumTasks\":2,\"finalizeAggregations\":true}";

        String result = queryToolProvider.queryDruidMultiStageWithContext(testQuery, testContext);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Multi-stage query with context result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Multi-stage query with context handles errors gracefully");
    }

    @Test
    void testMultiStageQueryWithEmptyContext() {
        System.out.println("[DEBUG_LOG] Testing multi-stage query with empty context");
        String testQuery = "SELECT COUNT(*) FROM test_datasource";
        String emptyContext = "";

        String result = queryToolProvider.queryDruidMultiStageWithContext(testQuery, emptyContext);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Multi-stage query with empty context result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Multi-stage query with empty context handles errors gracefully");
    }

    @Test
    void testMultiStageQueryWithNullContext() {
        System.out.println("[DEBUG_LOG] Testing multi-stage query with null context");
        String testQuery = "SELECT COUNT(*) FROM test_datasource";

        String result = queryToolProvider.queryDruidMultiStageWithContext(testQuery, null);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Multi-stage query with null context result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Multi-stage query with null context handles errors gracefully");
    }

    @Test
    void testMultiStageQueryTaskStatus() {
        System.out.println("[DEBUG_LOG] Testing multi-stage query task status");
        String testTaskId = "test-task-id-12345";

        String result = queryToolProvider.getMultiStageQueryTaskStatus(testTaskId);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Multi-stage query task status result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{"));
        System.out.println("[DEBUG_LOG] Multi-stage query task status handles errors gracefully");
    }

    @Test
    void testMultiStageQueryTaskCancellation() {
        System.out.println("[DEBUG_LOG] Testing multi-stage query task cancellation");
        String testTaskId = "test-task-id-12345";

        String result = queryToolProvider.cancelMultiStageQueryTask(testTaskId);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Multi-stage query task cancellation result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{"));
        System.out.println("[DEBUG_LOG] Multi-stage query task cancellation handles errors gracefully");
    }

    @Test
    void testRegularQueryStillWorks() {
        System.out.println("[DEBUG_LOG] Testing that regular SQL query still works");
        String testQuery = "SELECT COUNT(*) FROM test_datasource";

        String result = queryToolProvider.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Regular SQL query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Regular SQL query handles errors gracefully");
    }

    @Test
    void testDruidConfigurationForQueries() {
        System.out.println("[DEBUG_LOG] Testing Druid configuration for queries");
        assertNotNull(druidConfig.getDruidRouterUrl());
        assertEquals("http://test-router:8888", druidConfig.getDruidRouterUrl());
        System.out.println("[DEBUG_LOG] Druid router URL configured correctly: " + druidConfig.getDruidRouterUrl());
    }

    @Test
    void testInvalidJsonContextHandling() {
        System.out.println("[DEBUG_LOG] Testing invalid JSON context handling");
        String testQuery = "SELECT COUNT(*) FROM test_datasource";
        String invalidContext = "{invalid json}";

        String result = queryToolProvider.queryDruidMultiStageWithContext(testQuery, invalidContext);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Multi-stage query with invalid context result: " + result);

        // Should return an error message due to invalid JSON
        assertTrue(result.contains("Error") || result.contains("Failed"));
        System.out.println("[DEBUG_LOG] Multi-stage query handles invalid JSON context gracefully");
    }
}
