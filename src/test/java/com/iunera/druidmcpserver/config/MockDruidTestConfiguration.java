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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iunera.druidmcpserver.ingestion.tasks.TasksRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration that provides mock implementations for Druid services
 * when no Druid cluster is running. This allows tests to run without requiring
 * a real Druid instance.
 */
@TestConfiguration
@Profile("mock-druid")
public class MockDruidTestConfiguration {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    @Primary
    public TasksRepository mockTasksRepository() throws Exception {
        TasksRepository mockRepository = mock(TasksRepository.class);

        // Create sample JSON responses
        JsonNode sampleTaskList = objectMapper.readTree("""
            [
                {
                    "id": "sample-task-1",
                    "type": "index_parallel",
                    "status": "RUNNING",
                    "createdTime": "2024-01-01T10:00:00.000Z"
                },
                {
                    "id": "sample-task-2", 
                    "type": "index_parallel",
                    "status": "SUCCESS",
                    "createdTime": "2024-01-01T09:00:00.000Z"
                }
            ]
            """);

        JsonNode sampleTaskDetails = objectMapper.readTree("""
            {
                "id": "sample-task-1",
                "type": "index_parallel",
                "status": "RUNNING",
                "createdTime": "2024-01-01T10:00:00.000Z",
                "spec": {
                    "type": "index_parallel",
                    "ioConfig": {
                        "type": "index_parallel",
                        "inputSource": {
                            "type": "local",
                            "baseDir": "/tmp/data"
                        }
                    }
                }
            }
            """);

        JsonNode sampleTaskStatus = objectMapper.readTree("""
            {
                "task": "sample-task-1",
                "status": {
                    "id": "sample-task-1",
                    "status": "RUNNING",
                    "duration": 30000
                }
            }
            """);

        JsonNode sampleTaskReports = objectMapper.readTree("""
            {
                "ingestionStatsAndErrors": {
                    "taskId": "sample-task-1",
                    "payload": {
                        "ingestionState": "RUNNING",
                        "unparseableEvents": 0,
                        "rowStats": {
                            "processed": 1000,
                            "processedWithError": 0,
                            "thrownAway": 0,
                            "unparseable": 0
                        }
                    }
                }
            }
            """);

        JsonNode killTaskResponse = objectMapper.readTree("""
            {
                "task": "sample-task-1",
                "action": "shutdown"
            }
            """);

        // Mock all repository methods
        when(mockRepository.getRunningTasks()).thenReturn(sampleTaskList);
        when(mockRepository.getPendingTasks()).thenReturn(objectMapper.createArrayNode());
        when(mockRepository.getWaitingTasks()).thenReturn(objectMapper.createArrayNode());
        when(mockRepository.getCompleteTasks()).thenReturn(sampleTaskList);
        
        when(mockRepository.getTaskDetails(anyString())).thenReturn(sampleTaskDetails);
        when(mockRepository.getTaskIngestionSpec(anyString())).thenReturn(sampleTaskDetails.get("spec"));
        when(mockRepository.getTaskStatus(anyString())).thenReturn(sampleTaskStatus);
        when(mockRepository.getTaskReports(anyString())).thenReturn(sampleTaskReports);
        when(mockRepository.killTask(anyString())).thenReturn(killTaskResponse);
        
        when(mockRepository.getTaskLog(anyString())).thenReturn("Sample task log output for testing");
        when(mockRepository.getTaskLog(anyString(), anyLong())).thenReturn("Sample task log output from offset for testing");

        return mockRepository;
    }
}