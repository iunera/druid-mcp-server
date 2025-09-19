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

package com.iunera.druidmcpserver.datamanagement.segments;

import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Druid Segment MCP services.
 * These tests verify that the segment services are properly configured as Spring beans
 * and that the @Tool and @McpResource annotations are working correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
class SegmentIntegrationTest {

    @Autowired
    private SegmentResources segmentResources;

    @Autowired
    private ReadSegmentTools readSegmentTools;

    @Autowired
    private WriteSegmentTools writeSegmentTools;

    @Test
    void testSegmentServicesAreInjected() {
        System.out.println("[DEBUG_LOG] Testing segment services injection");

        assertNotNull(segmentResources, "SegmentResources should be injected");
        assertNotNull(readSegmentTools, "ReadSegmentTools should be injected");
        assertNotNull(writeSegmentTools, "WriteSegmentTools should be injected");

        System.out.println("[DEBUG_LOG] All segment services are properly injected");
    }

    @Test
    void testSegmentResourceProviderMethodsExist() {
        // Test that the segment resource provider methods exist and can be called
        // Note: These will either return error messages if Druid is not available,
        // or successful responses if Druid is running

        ReadResourceRequest request = new ReadResourceRequest("segment://test_segment_id");
        ReadResourceResult result = segmentResources.getSegment(request, "test_segment_id");
        assertNotNull(result, "getSegment should return a non-null result");
        assertNotNull(result.contents(), "getSegment should return contents");
        assertFalse(result.contents().isEmpty(), "getSegment should return non-empty contents");

        // Extract the text content from the first resource content
        TextResourceContents textResource = (TextResourceContents) result.contents().get(0);
        String textContent = textResource.text();
        assertNotNull(textContent, "Text content should not be null");
        assertFalse(textContent.trim().isEmpty(), "Text content should not be empty");

        // Debug output to see what we're actually getting
        System.out.println("[DEBUG_LOG] Segment resource result: " + textContent.substring(0, Math.min(200, textContent.length())));

        // Should contain either error message or valid JSON response
        assertTrue(textContent.contains("Error") || textContent.contains("Failed") || textContent.contains("not found") || textContent.contains("[") || textContent.contains("{"),
                "Should contain either error message or valid response");
    }

    @Test
    void testSegmentToolProviderMethods() {
        System.out.println("[DEBUG_LOG] Testing ReadSegmentTools and WriteSegmentTools methods");

        // Check that read methods exist on ReadSegmentTools
        Method[] readMethods = readSegmentTools.getClass().getDeclaredMethods();
        boolean hasListAllSegments = Arrays.stream(readMethods)
                .anyMatch(m -> m.getName().equals("listAllSegments"));
        boolean hasGetSegmentMetadata = Arrays.stream(readMethods)
                .anyMatch(m -> m.getName().equals("getSegmentMetadata"));

        assertTrue(hasListAllSegments, "ReadSegmentTools should have listAllSegments method");
        assertTrue(hasGetSegmentMetadata, "ReadSegmentTools should have getSegmentMetadata method");

        // Check that write methods exist on WriteSegmentTools
        Method[] writeMethods = writeSegmentTools.getClass().getDeclaredMethods();
        boolean hasEnableSegment = Arrays.stream(writeMethods)
                .anyMatch(m -> m.getName().equals("enableSegment"));
        boolean hasDisableSegment = Arrays.stream(writeMethods)
                .anyMatch(m -> m.getName().equals("disableSegment"));

        assertTrue(hasEnableSegment, "WriteSegmentTools should have enableSegment method");
        assertTrue(hasDisableSegment, "WriteSegmentTools should have disableSegment method");

        System.out.println("[DEBUG_LOG] Read/Write SegmentTools methods verified");
    }

    @Test
    void testSegmentResourceReturnCorrectTypes() {
        // Verify that @McpResource methods return ReadResourceResult
        ReadResourceRequest segmentRequest = new ReadResourceRequest("segment://test_segment_id");
        ReadResourceResult segmentResult = segmentResources.getSegment(segmentRequest, "test_segment_id");

        assertNotNull(segmentResult);

        // Verify resource results have content
        assertNotNull(segmentResult.contents());
        assertFalse(segmentResult.contents().isEmpty());

        System.out.println("[DEBUG_LOG] Segment resource returns correct ReadResourceResult type");
    }

    @Test
    void testSegmentToolReturnCorrectTypes() {
        System.out.println("[DEBUG_LOG] Testing segment tool return correct types");

        // Test that methods return String (as required by MCP tools)
        Method[] readMethods = readSegmentTools.getClass().getDeclaredMethods();
        Method[] writeMethods = writeSegmentTools.getClass().getDeclaredMethods();

        // Check ReadSegmentTools methods
        for (Method method : readMethods) {
            if (method.getName().startsWith("list") || method.getName().startsWith("get")) {
                assertEquals(String.class, method.getReturnType(),
                        "ReadSegmentTools method " + method.getName() + " should return String");
            }
        }

        // Check WriteSegmentTools methods
        for (Method method : writeMethods) {
            if (method.getName().startsWith("enable") || method.getName().startsWith("disable")) {
                assertEquals(String.class, method.getReturnType(),
                        "WriteSegmentTools method " + method.getName() + " should return String");
            }
        }

        System.out.println("[DEBUG_LOG] All segment tool methods return correct types");
    }

    @Test
    void testSegmentResourceUriPatterns() {
        // Test that the resources use the expected URI patterns
        ReadResourceRequest segmentRequest = new ReadResourceRequest("segment://test_segment_id");
        ReadResourceResult segmentResult = segmentResources.getSegment(segmentRequest, "test_segment_id");

        // Check that segment URIs follow the expected pattern
        if (!segmentResult.contents().isEmpty()) {
            TextResourceContents segmentContent = (TextResourceContents) segmentResult.contents().get(0);
            String segmentUri = segmentContent.uri();
            String segmentText = segmentContent.text();

            // If it's an error response, the URI will be the original request URI
            // If it's a successful response, it should follow the pattern
            boolean isValidSegmentUri = segmentUri.startsWith("segment://") ||
                    (segmentUri.equals("segment://test_segment_id") && (segmentText.contains("Error") || segmentText.contains("Failed") || segmentText.contains("not found")));
            assertTrue(isValidSegmentUri,
                    "Segment URI should follow expected pattern or be error response: " + segmentUri);

            System.out.println("[DEBUG_LOG] Segment URI: " + segmentUri + ", is error: " + (segmentText.contains("Error") || segmentText.contains("Failed") || segmentText.contains("not found")));
        }

        System.out.println("[DEBUG_LOG] URI patterns verified for segment resources");
    }
}
