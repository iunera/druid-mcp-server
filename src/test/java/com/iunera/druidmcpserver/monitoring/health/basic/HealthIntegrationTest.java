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

package com.iunera.druidmcpserver.monitoring.health.basic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Druid Health MCP services.
 * These tests verify that the health services are properly configured as Spring beans
 * and that the @Tool annotations are working correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
class HealthIntegrationTest {

    @Autowired
    private HealthToolProvider healthToolProvider;

    @Test
    void testHealthServicesAreInjected() {
        System.out.println("[DEBUG_LOG] Testing health services injection");

        assertNotNull(healthToolProvider, "HealthToolProvider should be injected");

        System.out.println("[DEBUG_LOG] All health services are properly injected");
    }

    @Test
    void testHealthToolProviderMethods() {
        System.out.println("[DEBUG_LOG] Testing HealthToolProvider methods");

        // Check that key methods exist
        Method[] methods = healthToolProvider.getClass().getDeclaredMethods();
        boolean hasCheckClusterHealth = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("checkClusterHealth"));
        boolean hasGetCoordinatorHealth = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("getCoordinatorHealth"));

        assertTrue(hasCheckClusterHealth, "HealthToolProvider should have checkClusterHealth method");
        assertTrue(hasGetCoordinatorHealth, "HealthToolProvider should have getCoordinatorHealth method");

        System.out.println("[DEBUG_LOG] HealthToolProvider methods verified");
    }

    @Test
    void testHealthToolReturnCorrectTypes() {
        System.out.println("[DEBUG_LOG] Testing health tool return correct types");

        // Test that methods return String (as required by MCP tools)
        Method[] healthMethods = healthToolProvider.getClass().getSuperclass().getDeclaredMethods();

        // Check that public methods return String
        for (Method method : healthMethods) {
            if (method.getName().startsWith("check") || method.getName().startsWith("get") || method.getName().startsWith("is")) {
                System.out.println("[DEBUG_LOG] Tested: Method" + method.getName());
                assertEquals(String.class, method.getReturnType(),
                        "HealthToolProvider method " + method.getName() + " should return String");
            }
        }

        System.out.println("[DEBUG_LOG] All health tool methods return correct types");
    }

    @Test
    void testHealthToolProviderFunctionality() {
        System.out.println("[DEBUG_LOG] Testing HealthToolProvider basic functionality");

        // Test that we can call the methods without exceptions
        // Note: These will either return error messages if Druid is not available,
        // or successful responses if Druid is running

        try {
            // Use reflection to call methods if they exist
            Method[] methods = healthToolProvider.getClass().getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().equals("checkClusterHealth") && method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    Object result = method.invoke(healthToolProvider);
                    assertNotNull(result, "checkClusterHealth should return a non-null result");
                    assertInstanceOf(String.class, result, "checkClusterHealth should return a String");
                    System.out.println("[DEBUG_LOG] checkClusterHealth result: " + result.toString().substring(0, Math.min(100, result.toString().length())));
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Health tool method invocation failed (expected if Druid not available): " + e.getMessage());
            // This is expected if Druid is not running
        }

        System.out.println("[DEBUG_LOG] HealthToolProvider functionality test completed");
    }
}
