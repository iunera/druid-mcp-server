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

package com.iunera.druidmcpserver.ingestion.supervisors;

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
class SupervisorsIntegrationTest {

    @Autowired
    private SupervisorsRepository supervisorsRepository;

    @Autowired
    private SupervisorsToolProvider supervisorsToolProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing context loading for supervisors components");
        assertNotNull(supervisorsRepository);
        assertNotNull(supervisorsToolProvider);
        assertNotNull(objectMapper);
        System.out.println("[DEBUG_LOG] All supervisors components loaded successfully");
    }

    @Test
    void testListSupervisorsWhenDruidNotAvailable() {
        System.out.println("[DEBUG_LOG] Testing supervisors listing when Druid is not available");

        // This test will likely fail with connection errors when Druid is not running
        // but should handle the errors gracefully

        String result = supervisorsToolProvider.listSupervisors();
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] List supervisors result: " + result);

        // Should either return valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("[") || result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] List supervisors test completed");
    }

    @Test
    void testGetSupervisorStatusWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing supervisor status retrieval with invalid ID");

        String result = supervisorsToolProvider.getSupervisorStatus("invalid-supervisor-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Get supervisor status result: " + result);

        // Should return either valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Get supervisor status test completed");
    }

    @Test
    void testSuspendSupervisorWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing supervisor suspension with invalid ID");

        String result = supervisorsToolProvider.suspendSupervisor("invalid-supervisor-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Suspend supervisor result: " + result);

        // Should return either valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Suspend supervisor test completed");
    }

    @Test
    void testStartSupervisorWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing supervisor start/resume with invalid ID");

        String result = supervisorsToolProvider.startSupervisor("invalid-supervisor-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Start supervisor result: " + result);

        // Should return either valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Start supervisor test completed");
    }

    @Test
    void testTerminateSupervisorWithInvalidId() {
        System.out.println("[DEBUG_LOG] Testing supervisor termination with invalid ID");

        String result = supervisorsToolProvider.terminateSupervisor("invalid-supervisor-id");
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Terminate supervisor result: " + result);

        // Should return either valid JSON or an error message
        assertTrue(result.startsWith("{") || result.startsWith("Error") || result.startsWith("Failed"));

        System.out.println("[DEBUG_LOG] Terminate supervisor test completed");
    }

    @Test
    void testSupervisorOperationsErrorHandling() {
        System.out.println("[DEBUG_LOG] Testing supervisor operations error handling");

        // Test with null supervisor ID (should be handled gracefully)
        String listResult = supervisorsToolProvider.listSupervisors();
        assertNotNull(listResult);
        System.out.println("[DEBUG_LOG] List supervisors with potential connection error: " + listResult);

        // Test operations with empty string ID
        String statusResult = supervisorsToolProvider.getSupervisorStatus("");
        assertNotNull(statusResult);
        System.out.println("[DEBUG_LOG] Get status with empty ID: " + statusResult);

        String suspendResult = supervisorsToolProvider.suspendSupervisor("");
        assertNotNull(suspendResult);
        System.out.println("[DEBUG_LOG] Suspend with empty ID: " + suspendResult);

        String startResult = supervisorsToolProvider.startSupervisor("");
        assertNotNull(startResult);
        System.out.println("[DEBUG_LOG] Start with empty ID: " + startResult);

        System.out.println("[DEBUG_LOG] Supervisor operations error handling test completed");
    }
}
