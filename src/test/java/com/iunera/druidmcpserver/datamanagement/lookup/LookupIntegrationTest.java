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

package com.iunera.druidmcpserver.datamanagement.lookup;

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
 * Integration tests for Druid Lookup MCP services.
 * These tests verify that the lookup services are properly configured as Spring beans
 * and that the @Tool and @McpResource annotations are working correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
class LookupIntegrationTest {

    @Autowired
    private LookupResources lookupResources;

    @Autowired
    private ReadLookupTools readLookupTools;

    @Autowired
    private WriteLookupTools writeLookupTools;

    @Test
    void testLookupServicesAreInjected() {
        System.out.println("[DEBUG_LOG] Testing lookup services injection");

        assertNotNull(lookupResources, "LookupResources should be injected");
        assertNotNull(readLookupTools, "ReadLookupTools should be injected");
        assertNotNull(writeLookupTools, "WriteLookupTools should be injected");

        System.out.println("[DEBUG_LOG] All lookup services are properly injected");
    }

    @Test
    void testLookupResourceProviderMethodsExist() {
        // Test that the lookup resource provider methods exist and can be called
        // Note: These will either return error messages if Druid is not available,
        // or successful responses if Druid is running

        ReadResourceRequest request = new ReadResourceRequest("lookup://test_tier/test_lookup");
        ReadResourceResult result = lookupResources.getLookup(request, "test_tier/test_lookup");
        assertNotNull(result, "getLookup should return a non-null result");
        assertNotNull(result.contents(), "getLookup should return contents");
        assertFalse(result.contents().isEmpty(), "getLookup should return non-empty contents");

        // Extract the text content from the first resource content
        TextResourceContents textResource = (TextResourceContents) result.contents().get(0);
        String textContent = textResource.text();
        assertNotNull(textContent, "Text content should not be null");
        assertFalse(textContent.trim().isEmpty(), "Text content should not be empty");

        // Debug output to see what we're actually getting
        System.out.println("[DEBUG_LOG] Lookup resource result: " + textContent.substring(0, Math.min(200, textContent.length())));

        // Should contain either error message or valid JSON response
        assertTrue(textContent.contains("Error") || textContent.contains("Failed") || textContent.contains("not found") || textContent.contains("[") || textContent.contains("{"),
                "Should contain either error message or valid response");
    }

    @Test
    void testLookupToolProviderMethods() {
        System.out.println("[DEBUG_LOG] Testing LookupTools methods after refactor");

        // ReadLookupTools should contain read-only operations
        Method[] readMethods = readLookupTools.getClass().getDeclaredMethods();
        boolean hasListLookups = Arrays.stream(readMethods)
                .anyMatch(m -> m.getName().equals("listLookups"));
        boolean hasGetLookupStatus = Arrays.stream(readMethods)
                .anyMatch(m -> m.getName().equals("getLookupStatus"));

        assertTrue(hasListLookups, "ReadLookupTools should have listLookups method");
        assertTrue(hasGetLookupStatus, "ReadLookupTools should have getLookupStatus method");

        // WriteLookupTools should contain write operations
        Method[] writeMethods = writeLookupTools.getClass().getDeclaredMethods();
        boolean hasCreateOrUpdateLookup = Arrays.stream(writeMethods)
                .anyMatch(m -> m.getName().equals("createOrUpdateLookup"));
        boolean hasDeleteLookup = Arrays.stream(writeMethods)
                .anyMatch(m -> m.getName().equals("deleteLookup"));

        assertTrue(hasCreateOrUpdateLookup, "WriteLookupTools should have createOrUpdateLookup method");
        assertTrue(hasDeleteLookup, "WriteLookupTools should have deleteLookup method");

        System.out.println("[DEBUG_LOG] ReadLookupTools and WriteLookupTools methods verified");
    }

    @Test
    void testLookupResourceReturnCorrectTypes() {
        // Verify that @McpResource methods return ReadResourceResult
        ReadResourceRequest lookupRequest = new ReadResourceRequest("lookup://test_tier/test_lookup");
        ReadResourceResult lookupResult = lookupResources.getLookup(lookupRequest, "test_tier/test_lookup");

        assertNotNull(lookupResult);

        // Verify resource results have content
        assertNotNull(lookupResult.contents());
        assertFalse(lookupResult.contents().isEmpty());

        System.out.println("[DEBUG_LOG] Lookup resource returns correct ReadResourceResult type");
    }

    @Test
    void testLookupToolReturnCorrectTypes() {
        System.out.println("[DEBUG_LOG] Testing lookup tool return correct types");

        // Test that methods return String (as required by MCP tools)
        Method[] lookupMethods = writeLookupTools.getClass().getSuperclass().getDeclaredMethods();

        // Check that public methods return String
        for (Method method : lookupMethods) {
            if (method.getName().startsWith("list") || method.getName().startsWith("get") || method.getName().startsWith("create") || method.getName().startsWith("delete")) {
                System.out.println("[DEBUG_LOG] Tested: Method" + method.getName());
                assertEquals(String.class, method.getReturnType(),
                        "WriteLookupTools method " + method.getName() + " should return String");
            }
        }

        System.out.println("[DEBUG_LOG] All lookup tool methods return correct types");
    }

    @Test
    void testLookupResourceUriPatterns() {
        // Test that the resources use the expected URI patterns
        ReadResourceRequest lookupRequest = new ReadResourceRequest("lookup://test_tier/test_lookup");
        ReadResourceResult lookupResult = lookupResources.getLookup(lookupRequest, "test_tier/test_lookup");

        // Check that lookup URIs follow the expected pattern
        if (!lookupResult.contents().isEmpty()) {
            TextResourceContents lookupContent = (TextResourceContents) lookupResult.contents().get(0);
            String lookupUri = lookupContent.uri();
            String lookupText = lookupContent.text();

            // If it's an error response, the URI will be the original request URI
            // If it's a successful response, it should follow the pattern
            boolean isValidLookupUri = lookupUri.startsWith("lookup://") ||
                    (lookupUri.equals("lookup://test_tier/test_lookup") && (lookupText.contains("Error") || lookupText.contains("Failed") || lookupText.contains("not found")));
            assertTrue(isValidLookupUri,
                    "Lookup URI should follow expected pattern or be error response: " + lookupUri);

            System.out.println("[DEBUG_LOG] Lookup URI: " + lookupUri + ", is error: " + (lookupText.contains("Error") || lookupText.contains("Failed") || lookupText.contains("not found")));
        }

        System.out.println("[DEBUG_LOG] URI patterns verified for lookup resources");
    }
}
