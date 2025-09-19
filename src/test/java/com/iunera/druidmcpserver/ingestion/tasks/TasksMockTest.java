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
import com.iunera.druidmcpserver.config.MockDruidTestConfiguration;
import com.iunera.druidmcpserver.config.TestProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Tasks functionality using mock Druid services.
 * This test runs without requiring a real Druid cluster.
 */
@SpringBootTest
@ActiveProfiles(TestProfiles.MOCK_DRUID)
@Import(MockDruidTestConfiguration.class)
class TasksMockTest {

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private ReadTasksTools readTasksTools;

    @Autowired
    private WriteTasksTools writeTasksTools;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing context loading with mock Druid services");
        assertNotNull(tasksRepository);
        assertNotNull(writeTasksTools);
        assertNotNull(objectMapper);
        System.out.println("[DEBUG_LOG] All components loaded successfully with mock configuration");
    }

    @Test
    void testListTasksWithMockData() {
        System.out.println("[DEBUG_LOG] Testing task listing with mock data");

        String runningTasks = readTasksTools.listRunningTasks();
        assertNotNull(runningTasks);
        assertTrue(runningTasks.contains("sample-task-1"));
        assertTrue(runningTasks.contains("index_parallel"));
        System.out.println("[DEBUG_LOG] Running tasks result: " + runningTasks);

        String pendingTasks = readTasksTools.listPendingTasks();
        assertNotNull(pendingTasks);
        assertTrue(pendingTasks.equals("[]")); // Empty array for pending tasks
        System.out.println("[DEBUG_LOG] Pending tasks result: " + pendingTasks);

        String waitingTasks = readTasksTools.listWaitingTasks();
        assertNotNull(waitingTasks);
        assertTrue(waitingTasks.equals("[]")); // Empty array for waiting tasks
        System.out.println("[DEBUG_LOG] Waiting tasks result: " + waitingTasks);

        String completedTasks = readTasksTools.listCompletedTasks();
        assertNotNull(completedTasks);
        assertTrue(completedTasks.contains("sample-task-1"));
        System.out.println("[DEBUG_LOG] Completed tasks result: " + completedTasks);

        System.out.println("[DEBUG_LOG] Task listing tests with mock data completed successfully");
    }

    @Test
    void testTaskOperationsWithMockData() {
        System.out.println("[DEBUG_LOG] Testing task operations with mock data");

        String taskId = "sample-task-1";

        // Test getting task details
        String taskDetails = readTasksTools.getTaskRawDetails(taskId);
        assertNotNull(taskDetails);
        assertTrue(taskDetails.contains(taskId));
        assertTrue(taskDetails.contains("index_parallel"));
        System.out.println("[DEBUG_LOG] Task details result: " + taskDetails);

        // Test getting task status
        String taskStatus = readTasksTools.getTaskStatus(taskId);
        assertNotNull(taskStatus);
        assertTrue(taskStatus.contains(taskId));
        assertTrue(taskStatus.contains("RUNNING"));
        System.out.println("[DEBUG_LOG] Task status result: " + taskStatus);

        // Test getting task ingestion spec
        String ingestionSpec = readTasksTools.getTaskIngestionSpec(taskId);
        assertNotNull(ingestionSpec);
        assertTrue(ingestionSpec.contains("index_parallel"));
        assertTrue(ingestionSpec.contains("ioConfig"));
        System.out.println("[DEBUG_LOG] Ingestion spec result: " + ingestionSpec);

        // Test getting task reports
        String taskReports = readTasksTools.getTaskReports(taskId);
        assertNotNull(taskReports);
        assertTrue(taskReports.contains("ingestionStatsAndErrors"));
        assertTrue(taskReports.contains("processed"));
        System.out.println("[DEBUG_LOG] Task reports result: " + taskReports);

        // Test getting task log
        String taskLog = readTasksTools.getTaskLog(taskId);
        assertNotNull(taskLog);
        assertTrue(taskLog.contains("Sample task log output"));
        System.out.println("[DEBUG_LOG] Task log result: " + taskLog);

        // Test getting task log with offset
        String taskLogWithOffset = readTasksTools.getTaskLogWithOffset(taskId, 100);
        assertNotNull(taskLogWithOffset);
        assertTrue(taskLogWithOffset.contains("Sample task log output from offset"));
        System.out.println("[DEBUG_LOG] Task log with offset result: " + taskLogWithOffset);

        // Test killing task
        String killResult = writeTasksTools.killTask(taskId);
        assertNotNull(killResult);
        assertTrue(killResult.contains(taskId));
        assertTrue(killResult.contains("shutdown"));
        System.out.println("[DEBUG_LOG] Kill task result: " + killResult);

        System.out.println("[DEBUG_LOG] Task operations tests with mock data completed successfully");
    }

    @Test
    void testTaskOperationsReturnValidJson() {
        System.out.println("[DEBUG_LOG] Testing that task operations return valid JSON");

        String taskId = "sample-task-1";

        // Test that all operations return valid JSON (not error messages)
        String runningTasks = readTasksTools.listRunningTasks();
        assertTrue(runningTasks.startsWith("[") || runningTasks.startsWith("{"));
        assertFalse(runningTasks.startsWith("Error"));

        String taskDetails = readTasksTools.getTaskRawDetails(taskId);
        assertTrue(taskDetails.startsWith("{"));
        assertFalse(taskDetails.startsWith("Error"));

        String taskStatus = readTasksTools.getTaskStatus(taskId);
        assertTrue(taskStatus.startsWith("{"));
        assertFalse(taskStatus.startsWith("Error"));

        String ingestionSpec = readTasksTools.getTaskIngestionSpec(taskId);
        assertTrue(ingestionSpec.startsWith("{"));
        assertFalse(ingestionSpec.startsWith("Error"));

        String taskReports = readTasksTools.getTaskReports(taskId);
        assertTrue(taskReports.startsWith("{"));
        assertFalse(taskReports.startsWith("Error"));

        String killResult = writeTasksTools.killTask(taskId);
        assertTrue(killResult.startsWith("{"));
        assertFalse(killResult.startsWith("Error"));

        System.out.println("[DEBUG_LOG] All operations return valid JSON instead of error messages");
    }
}
