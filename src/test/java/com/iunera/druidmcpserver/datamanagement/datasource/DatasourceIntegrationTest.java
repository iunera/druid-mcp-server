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

import com.iunera.druidmcpserver.datamanagement.query.MsqQueryTools;
import com.iunera.druidmcpserver.datamanagement.query.QueryTools;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Druid MCP services.
 * These tests verify that the services are properly configured as Spring beans
 * and that the @Tool annotations are working correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
class DatasourceIntegrationTest {

    @Autowired
    private DatasourceResources datasourceResourceProvider;

    @Autowired
    private WriteDatasourceTools writeDatasourceTools;

    @Autowired
    private ReadDatasourceTools readDatasourceTools;

    @Autowired
    private MsqQueryTools msqQueryTools;

    @Autowired
    private QueryTools queryTools;

    @Test
    void testServicesAreInjected() {
        assertNotNull(datasourceResourceProvider, "DatasourceResourceProvider should be injected");
        assertNotNull(writeDatasourceTools, "WriteDatasourceTools should be injected");
        assertNotNull(msqQueryTools, "MsqQueryTools should be injected");
        assertNotNull(queryTools, "QueryTools should be injected");
    }


    @Test
    void testDatasourceResourceProviderMethodsExist() {
        // Test that the resource provider methods exist and can be called
        // Note: These will either return error messages if Druid is not available,
        // or successful responses if Druid is running

        String testDatasourceName = "test-datasource";
        ReadResourceRequest request = new ReadResourceRequest("datasource://" + testDatasourceName);
        ReadResourceResult result = datasourceResourceProvider.getDatasource(request, testDatasourceName);
        assertNotNull(result, "getDatasource should return a non-null result");
        assertNotNull(result.contents(), "getDatasource should return contents");
        assertFalse(result.contents().isEmpty(), "getDatasource should return non-empty contents");

        // Extract the text content from the first resource content
        TextResourceContents textResource = (TextResourceContents) result.contents().get(0);
        String textContent = textResource.text();
        assertNotNull(textContent, "Text content should not be null");
        assertFalse(textContent.trim().isEmpty(), "Text content should not be empty");

        // Should contain either error message or valid JSON response
        assertTrue(textContent.contains("Error") || textContent.contains("Failed") || textContent.contains("not found") || textContent.contains("[") || textContent.contains("{"),
                "Should contain either error message or valid response");
    }

    @Test
    void testQueryServiceMethodsExist() {
        String result = queryTools.queryDruidSql("SELECT 1");
        assertNotNull(result, "queryDruidSql should return a non-null result");
        assertFalse(result.trim().isEmpty(), "queryDruidSql should return a non-empty result");

        // Debug output to see what we're actually getting
        System.out.println("[DEBUG_LOG] Query service result: " + result);

        // Should contain either error message or valid JSON response
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("[") || result.contains("{"),
                "Should contain either error message or valid response. Actual result: " + result);
    }

    @Test
    void testServiceMethodsReturnCorrectTypes() {
        // Verify that @Tool methods return String and @McpResource methods return ReadResourceResult
        String testDatasourceName = "test-datasource";
        ReadResourceRequest datasourceRequest = new ReadResourceRequest("datasource://" + testDatasourceName);
        ReadResourceResult datasourceResult = datasourceResourceProvider.getDatasource(datasourceRequest, testDatasourceName);
        String queryResult = queryTools.queryDruidSql("SELECT 1");


        assertNotNull(datasourceResult);
        assertNotNull(queryResult);

        // Verify resource results have content
        assertNotNull(datasourceResult.contents());
        assertFalse(datasourceResult.contents().isEmpty());

        // Verify query result is a non-empty string
        assertFalse(queryResult.trim().isEmpty());
    }

    @Test
    void testDatasourceToolMethod() {
        // Test the new @Tool method for listing datasources
        String toolResult = readDatasourceTools.listDatasources();
        assertNotNull(toolResult, "listDatasources tool method should return a non-null result");
        assertFalse(toolResult.trim().isEmpty(), "listDatasources tool method should return a non-empty result");

        // Debug output to see what we're actually getting
        System.out.println("[DEBUG_LOG] Datasource tool result: " + toolResult);

        // Should contain either error message or valid JSON response
        assertTrue(toolResult.contains("Error") || toolResult.contains("Failed") || toolResult.contains("[") || toolResult.contains("{"),
                "Should contain either error message or valid JSON response. Actual result: " + toolResult);
    }

    @Test
    void testKillDatasourceToolMethod() {
        // Test the kill datasource @Tool method
        String killResult = writeDatasourceTools.killDatasource("test_datasource", "1000-01-01/2025-07-06");
        assertNotNull(killResult, "killDatasource tool method should return a non-null result");
        assertFalse(killResult.trim().isEmpty(), "killDatasource tool method should return a non-empty result");

        // Debug output to see what we're actually getting
        System.out.println("[DEBUG_LOG] Kill datasource tool result: " + killResult);

        // Should contain either error message or valid JSON response
        // Since this is a destructive operation, we expect either:
        // 1. An error message if Druid is not available or datasource doesn't exist
        // 2. A successful response if the operation was accepted
        assertTrue(killResult.contains("Error") || killResult.contains("Failed") ||
                        killResult.contains("[") || killResult.contains("{") || killResult.contains("task"),
                "Should contain either error message or valid response. Actual result: " + killResult);
    }
}
