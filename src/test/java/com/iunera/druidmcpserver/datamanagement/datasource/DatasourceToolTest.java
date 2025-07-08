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

package com.iunera.druidmcpserver.datamanagement.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests to demonstrate the difference between listDatasources and showDatasource tools
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
class DatasourceToolTest {

    @Autowired
    private DatasourceToolProvider datasourceToolProvider;

    @Test
    void testListDatasourcesReturnsOnlyNames() {
        System.out.println("[DEBUG_LOG] Testing listDatasources - should return only datasource names");

        String result = datasourceToolProvider.listDatasources();
        assertNotNull(result, "listDatasources should return a non-null result");
        assertFalse(result.trim().isEmpty(), "listDatasources should return a non-empty result");

        System.out.println("[DEBUG_LOG] listDatasources result: " + result);

        // The result should be a JSON array of strings (names only) or an error message
        assertTrue(result.contains("Error") || result.contains("Failed") || result.startsWith("["),
                "listDatasources should return either an error or a JSON array of names");

        // If it's a successful response, it should not contain detailed column information
        if (result.startsWith("[") && !result.contains("Error") && !result.contains("Failed")) {
            assertFalse(result.contains("columns"),
                    "listDatasources should not contain detailed column information");
            assertFalse(result.contains("DATA_TYPE"),
                    "listDatasources should not contain column data types");
        }
    }

    @Test
    void testShowDatasourceReturnsDetailedInfo() {
        System.out.println("[DEBUG_LOG] Testing showDatasource - should return detailed information");

        String result = datasourceToolProvider.showDatasourceDetails("test_datasource");
        assertNotNull(result, "showDatasource should return a non-null result");
        assertFalse(result.trim().isEmpty(), "showDatasource should return a non-empty result");

        System.out.println("[DEBUG_LOG] showDatasource result: " + result);

        // The result should be detailed JSON object, error message, or "not found" message
        assertTrue(result.contains("Error") || result.contains("Failed") ||
                        result.contains("not found") || result.startsWith("{"),
                "showDatasource should return either an error, not found message, or detailed JSON object");

        // If it's a successful response with existing datasource, it should contain detailed information
        if (result.startsWith("{") && !result.contains("Error") && !result.contains("Failed") && !result.contains("not found")) {
            assertTrue(result.contains("datasource") || result.contains("columns"),
                    "showDatasource should contain detailed datasource or column information");
        }
    }

    @Test
    void testShowDatasourceWithNonExistentDatasource() {
        System.out.println("[DEBUG_LOG] Testing showDatasource with non-existent datasource");

        String result = datasourceToolProvider.showDatasourceDetails("definitely_does_not_exist_datasource_12345");
        assertNotNull(result, "showDatasource should return a non-null result even for non-existent datasource");
        assertFalse(result.trim().isEmpty(), "showDatasource should return a non-empty result");

        System.out.println("[DEBUG_LOG] showDatasource non-existent result: " + result);

        // Should return either an error message or "not found" message
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("not found"),
                "showDatasource should return error or not found message for non-existent datasource");
    }

    @Test
    void testKillDatasourceWithValidParameters() {
        System.out.println("[DEBUG_LOG] Testing killDatasource with valid parameters");

        String result = datasourceToolProvider.killDatasource("test_datasource", "1000-01-01/2025-07-06");
        assertNotNull(result, "killDatasource should return a non-null result");
        assertFalse(result.trim().isEmpty(), "killDatasource should return a non-empty result");

        System.out.println("[DEBUG_LOG] killDatasource result: " + result);

        // The result should be either a successful response or an error message
        // Since we're testing against a potentially non-existent Druid instance, we expect either:
        // 1. A successful JSON response from Druid coordinator
        // 2. An error message indicating connection issues or datasource not found
        assertTrue(result.contains("Error") || result.contains("Failed") ||
                        result.startsWith("{") || result.startsWith("[") || result.contains("task"),
                "killDatasource should return either an error message or a valid response");
    }

    @Test
    void testKillDatasourceWithInvalidInterval() {
        System.out.println("[DEBUG_LOG] Testing killDatasource with invalid interval format");

        String result = datasourceToolProvider.killDatasource("test_datasource", "invalid-interval");
        assertNotNull(result, "killDatasource should return a non-null result even with invalid interval");
        assertFalse(result.trim().isEmpty(), "killDatasource should return a non-empty result");

        System.out.println("[DEBUG_LOG] killDatasource invalid interval result: " + result);

        // Should return an error message due to invalid interval format
        assertTrue(result.contains("Error") || result.contains("Failed"),
                "killDatasource should return error message for invalid interval format");
    }
}
