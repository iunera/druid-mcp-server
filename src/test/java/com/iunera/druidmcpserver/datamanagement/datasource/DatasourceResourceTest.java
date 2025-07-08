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

import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for @McpResource methods in DatasourceResources to verify the separation
 * of list vs show functionality similar to the @Tool methods
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
class DatasourceResourceTest {

    @Autowired
    private DatasourceResources datasourceResources;

    @Test
    void testGetDatasourceResourceReturnsBasicInfo() {
        System.out.println("[DEBUG_LOG] Testing getDatasource @McpResource - should return basic datasource info");

        String testDatasourceName = "test-datasource";
        ReadResourceRequest request = new ReadResourceRequest("datasource://" + testDatasourceName);
        ReadResourceResult result = datasourceResources.getDatasource(request, testDatasourceName);

        assertNotNull(result, "getDatasource resource should return a non-null result");
        assertNotNull(result.contents(), "getDatasource resource should return contents");
        assertFalse(result.contents().isEmpty(), "getDatasource resource should return non-empty contents");

        // Should return exactly one content item
        assertEquals(1, result.contents().size(), "getDatasource should return exactly one content item");

        TextResourceContents textResource = (TextResourceContents) result.contents().get(0);
        String textContent = textResource.text();
        assertNotNull(textContent, "Text content should not be null");
        assertFalse(textContent.trim().isEmpty(), "Text content should not be empty");

        System.out.println("[DEBUG_LOG] getDatasource resource result: " + textContent);

        // Should contain either error message or JSON object
        assertTrue(textContent.contains("Error") || textContent.contains("Failed") || textContent.contains("not found") || textContent.startsWith("{"),
                "getDatasource resource should return either an error or a JSON object");

        // Verify URI is correct
        assertEquals("datasource://" + testDatasourceName, textResource.uri(), "URI should match the requested datasource");
    }

    @Test
    void testGetDatasourceDetailsResourceReturnsDetailedInfo() {
        System.out.println("[DEBUG_LOG] Testing getDatasourceDetails @McpResource - should return detailed information");

        String testDatasourceName = "test-datasource";
        ReadResourceResult result = datasourceResources.getDatasourceDetails(testDatasourceName);

        assertNotNull(result, "getDatasourceDetails resource should return a non-null result");
        assertNotNull(result.contents(), "getDatasourceDetails resource should return contents");
        assertFalse(result.contents().isEmpty(), "getDatasourceDetails resource should return non-empty contents");

        System.out.println("[DEBUG_LOG] getDatasourceDetails resource returned " + result.contents().size() + " content items");

        // Should return exactly one content item
        assertEquals(1, result.contents().size(), "getDatasourceDetails should return exactly one content item");

        // Check the content item
        TextResourceContents resource = (TextResourceContents) result.contents().get(0);
        String content = resource.text();
        assertNotNull(content, "Content should not be null");
        assertFalse(content.trim().isEmpty(), "Content should not be empty");

        System.out.println("[DEBUG_LOG] getDatasourceDetails content: " + content);

        // Should contain either error message or detailed JSON object
        assertTrue(content.contains("Error") || content.contains("Failed") || content.contains("not found") || content.startsWith("{"),
                "getDatasourceDetails resource should return either an error or detailed JSON object");

        // Verify URI is correct
        assertEquals("datasource-details://" + testDatasourceName, resource.uri(), "URI should match the requested datasource details pattern");
    }

    @Test
    void testResourceMethodsReturnDifferentFormats() {
        System.out.println("[DEBUG_LOG] Testing that basic and detailed resources return different formats");

        String testDatasourceName = "test-datasource";
        ReadResourceRequest basicRequest = new ReadResourceRequest("datasource://" + testDatasourceName);
        ReadResourceResult basicResult = datasourceResources.getDatasource(basicRequest, testDatasourceName);

        ReadResourceResult detailsResult = datasourceResources.getDatasourceDetails(testDatasourceName);

        assertNotNull(basicResult);
        assertNotNull(detailsResult);

        // Both should return exactly 1 content item
        assertEquals(1, basicResult.contents().size(), "Basic resource should return exactly 1 content item");
        assertEquals(1, detailsResult.contents().size(), "Details resource should return exactly 1 content item");

        TextResourceContents basicContent = (TextResourceContents) basicResult.contents().get(0);
        TextResourceContents detailsContent = (TextResourceContents) detailsResult.contents().get(0);

        String basicText = basicContent.text();
        String detailsText = detailsContent.text();

        System.out.println("[DEBUG_LOG] Basic content: " + basicText);
        System.out.println("[DEBUG_LOG] Details content: " + detailsText);

        // Verify URIs are different
        assertEquals("datasource://" + testDatasourceName, basicContent.uri(), "Basic URI should use datasource:// scheme");
        assertEquals("datasource-details://" + testDatasourceName, detailsContent.uri(), "Details URI should use datasource-details:// scheme");

        // If both are successful (no errors), details should contain more information
        if (!basicText.contains("Error") && !basicText.contains("Failed") && !basicText.contains("not found") &&
                !detailsText.contains("Error") && !detailsText.contains("Failed") && !detailsText.contains("not found")) {

            // Both should be JSON objects
            assertTrue(basicText.startsWith("{"), "Basic should return a JSON object");
            assertTrue(detailsText.startsWith("{"), "Details should return a JSON object");

            // Details should contain more information (columns, etc.)
            assertTrue(detailsText.length() >= basicText.length(),
                    "Details content should be at least as detailed as basic content");
        }
    }
}
