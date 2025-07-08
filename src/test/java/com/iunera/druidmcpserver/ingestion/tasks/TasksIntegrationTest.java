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

package com.iunera.druidmcpserver.ingestion.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
class TasksIntegrationTest {

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private TasksToolProvider tasksToolProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing context loading for tasks components");
        assertNotNull(tasksRepository);
        assertNotNull(tasksToolProvider);
        assertNotNull(objectMapper);
        System.out.println("[DEBUG_LOG] All tasks components loaded successfully");
    }

    @Test
    void testListTasksWhenDruidNotAvailable() {
        System.out.println("[DEBUG_LOG] Testing task listing when Druid is not available");

        // These tests will likely fail with connection errors when Druid is not running
        // but should handle the errors gracefully

        String runningTasks = tasksToolProvider.listRunningTasks();
        assertNotNull(runningTasks);
        System.out.println("[DEBUG_LOG] Running tasks result: " + runningTasks);

        String pendingTasks = tasksToolProvider.listPendingTasks();
        assertNotNull(pendingTasks);
        System.out.println("[DEBUG_LOG] Pending tasks result: " + pendingTasks);

        String waitingTasks = tasksToolProvider.listWaitingTasks();
        assertNotNull(waitingTasks);
        System.out.println("[DEBUG_LOG] Waiting tasks result: " + waitingTasks);

        String completedTasks = tasksToolProvider.listCompletedTasks();
        assertNotNull(completedTasks);
        System.out.println("[DEBUG_LOG] Completed tasks result: " + completedTasks);

        System.out.println("[DEBUG_LOG] Task listing tests completed");
    }


    @Test
    void testKillTaskWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing task killing with invalid ID");

        String result = tasksToolProvider.killTask("invalid-task-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Kill task result: " + result);

        // Should return either valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Kill task test completed");
    }

    @Test
    void testGetTaskRawDetailsWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing task raw details retrieval with invalid ID");

        String result = tasksToolProvider.getTaskRawDetails("invalid-task-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Get task raw details result: " + result);

        // Should return either valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Get task raw details test completed");
    }

    @Test
    void testGetTaskIngestionSpecWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing task ingestion spec retrieval with invalid ID");

        String result = tasksToolProvider.getTaskIngestionSpec("invalid-task-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Get task ingestion spec result: " + result);

        // Should return either valid JSON, null message, or an error message
        assertTrue(result.startsWith("{") || result.startsWith("No ingestion spec") ||
                result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Get task ingestion spec test completed");
    }

    @Test
    void testGetTaskReportsWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing task reports retrieval with invalid ID");

        String result = tasksToolProvider.getTaskReports("invalid-task-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Get task reports result: " + result);

        // Should return either valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("[") ||
                result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Get task reports test completed");
    }

    @Test
    void testGetTaskLogWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing task log retrieval with invalid ID");

        String result = tasksToolProvider.getTaskLog("invalid-task-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Get task log result: " + result);

        // Should return either log content or an error message
        assertTrue(result.length() > 0);

        System.out.println("[DEBUG_LOG] Get task log test completed");
    }

    @Test
    void testGetTaskLogWithOffsetAndInvalidId() {
        System.out.println("[DEBUG_LOG] Testing task log with offset retrieval with invalid ID");

        String result = tasksToolProvider.getTaskLogWithOffset("invalid-task-id", 100L);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Get task log with offset result: " + result);

        // Should return either log content or an error message
        assertTrue(result.length() > 0);

        System.out.println("[DEBUG_LOG] Get task log with offset test completed");
    }

    @Test
    void testGetTaskStatusWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing task status retrieval with invalid ID");

        String result = tasksToolProvider.getTaskStatus("invalid-task-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Get task status result: " + result);

        // Should return either valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Get task status test completed");
    }

    @Test
    void testTaskOperationsErrorHandling() {
        System.out.println("[DEBUG_LOG] Testing task operations error handling");

        // Test operations with empty string ID
        String killResult = tasksToolProvider.killTask("");
        assertNotNull(killResult);
        System.out.println("[DEBUG_LOG] Kill with empty ID: " + killResult);

        String detailsResult = tasksToolProvider.getTaskRawDetails("");
        assertNotNull(detailsResult);
        System.out.println("[DEBUG_LOG] Get details with empty ID: " + detailsResult);

        String specResult = tasksToolProvider.getTaskIngestionSpec("");
        assertNotNull(specResult);
        System.out.println("[DEBUG_LOG] Get spec with empty ID: " + specResult);

        String reportsResult = tasksToolProvider.getTaskReports("");
        assertNotNull(reportsResult);
        System.out.println("[DEBUG_LOG] Get reports with empty ID: " + reportsResult);

        String logResult = tasksToolProvider.getTaskLog("");
        assertNotNull(logResult);
        System.out.println("[DEBUG_LOG] Get log with empty ID: " + logResult);

        String statusResult = tasksToolProvider.getTaskStatus("");
        assertNotNull(statusResult);
        System.out.println("[DEBUG_LOG] Get status with empty ID: " + statusResult);

        System.out.println("[DEBUG_LOG] Task operations error handling test completed");
    }

    @Test
    void testTaskLogOffsetValidation() {
        System.out.println("[DEBUG_LOG] Testing task log offset validation");

        // Test with negative offset
        String result1 = tasksToolProvider.getTaskLogWithOffset("test-task", -1L);
        assertNotNull(result1);
        System.out.println("[DEBUG_LOG] Get log with negative offset: " + result1);

        // Test with zero offset
        String result2 = tasksToolProvider.getTaskLogWithOffset("test-task", 0L);
        assertNotNull(result2);
        System.out.println("[DEBUG_LOG] Get log with zero offset: " + result2);

        // Test with large offset
        String result3 = tasksToolProvider.getTaskLogWithOffset("test-task", 999999L);
        assertNotNull(result3);
        System.out.println("[DEBUG_LOG] Get log with large offset: " + result3);

        System.out.println("[DEBUG_LOG] Task log offset validation test completed");
    }
}
