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

package com.iunera.druidmcpserver.ingestion.spec;

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
class IngestionSpecIntegrationTest {

    @Autowired
    private IngestionSpecRepository ingestionSpecRepository;

    @Autowired
    private IngestionSpecTools ingestionSpecTools;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing context loading for ingestion spec components");
        assertNotNull(ingestionSpecRepository);
        assertNotNull(ingestionSpecTools);
        assertNotNull(objectMapper);
        System.out.println("[DEBUG_LOG] All ingestion spec components loaded successfully");
    }

    @Test
    void testCreateBatchIngestionTemplate() {
        System.out.println("[DEBUG_LOG] Testing batch ingestion template creation");

        String template = ingestionSpecTools.createBatchIngestionTemplate(
                "test-datasource",
                "local",
                "/path/to/data"
        );

        assertNotNull(template);
        assertFalse(template.startsWith("Failed"));
        assertFalse(template.startsWith("Error"));

        System.out.println("[DEBUG_LOG] Generated template: " + template);

        // Verify the template contains expected fields
        assertTrue(template.contains("test-datasource"));
        assertTrue(template.contains("index_parallel"));
        assertTrue(template.contains("dataSchema"));
        assertTrue(template.contains("ioConfig"));
        assertTrue(template.contains("tuningConfig"));

        System.out.println("[DEBUG_LOG] Batch ingestion template creation test passed");
    }

    @Test
    void testCreateIngestionSpecWithInvalidJson() {
        System.out.println("[DEBUG_LOG] Testing ingestion spec creation with invalid JSON");

        String result = ingestionSpecTools.createIngestionSpec("invalid json");

        assertNotNull(result);
        assertTrue(result.startsWith("Failed to process ingestion spec"));

        System.out.println("[DEBUG_LOG] Invalid JSON test result: " + result);
        System.out.println("[DEBUG_LOG] Invalid JSON handling test passed");
    }

    @Test
    void testCreateIngestionSpecWithMissingType() {
        System.out.println("[DEBUG_LOG] Testing ingestion spec creation with missing type field");

        String invalidSpec = "{\"spec\": {\"dataSchema\": {}}}";
        String result = ingestionSpecTools.createIngestionSpec(invalidSpec);

        assertNotNull(result);
        assertTrue(result.contains("must contain a 'type' field"));

        System.out.println("[DEBUG_LOG] Missing type test result: " + result);
        System.out.println("[DEBUG_LOG] Missing type validation test passed");
    }

    @Test
    void testCreateIngestionSpecWithMissingSpec() {
        System.out.println("[DEBUG_LOG] Testing ingestion spec creation with missing spec field");

        String invalidSpec = "{\"type\": \"index_parallel\"}";
        String result = ingestionSpecTools.createIngestionSpec(invalidSpec);

        assertNotNull(result);
        assertTrue(result.contains("must contain a 'spec' field"));

        System.out.println("[DEBUG_LOG] Missing spec test result: " + result);
        System.out.println("[DEBUG_LOG] Missing spec validation test passed");
    }


    @Test
    void testCreateIngestionSpecWithValidStructure() {
        System.out.println("[DEBUG_LOG] Testing ingestion spec creation with valid structure");

        // Create a valid ingestion spec structure (but won't actually submit due to no Druid connection)
        String validSpec = """
                {
                    "type": "index_parallel",
                    "spec": {
                        "dataSchema": {
                            "dataSource": "test-datasource",
                            "timestampSpec": {
                                "column": "__time",
                                "format": "auto"
                            },
                            "dimensionsSpec": {
                                "dimensions": []
                            },
                            "granularitySpec": {
                                "type": "uniform",
                                "segmentGranularity": "DAY",
                                "queryGranularity": "HOUR",
                                "rollup": false
                            }
                        },
                        "ioConfig": {
                            "type": "index_parallel",
                            "inputSource": {
                                "type": "local",
                                "baseDir": "/tmp/test",
                                "filter": "*.json"
                            },
                            "inputFormat": {
                                "type": "json"
                            }
                        },
                        "tuningConfig": {
                            "type": "index_parallel",
                            "maxRowsPerSegment": 5000000,
                            "maxRowsInMemory": 1000000
                        }
                    }
                }
                """;

        String result = ingestionSpecTools.createIngestionSpec(validSpec);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Valid spec submission result: " + result);

        // The result will likely be an error due to no Druid connection, but it should not be a validation error
        assertFalse(result.contains("must contain a 'type' field"));
        assertFalse(result.contains("must contain a 'spec' field"));

        System.out.println("[DEBUG_LOG] Valid ingestion spec structure test completed");
    }
}
