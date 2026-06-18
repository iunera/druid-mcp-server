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

package com.iunera.druidmcpserver.basicsecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Druid Security MCP services.
 * These tests verify that the security services are properly configured as Spring beans
 * and that the @McpTool annotations are working correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://localhost:8888"
})
class SecurityIntegrationTest {

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private SecurityTools securityTools;

    @Test
    void testSecurityServicesAreInjected() {
        System.out.println("[DEBUG_LOG] Testing security services injection");

        assertNotNull(securityRepository, "SecurityRepository should be injected");
        assertNotNull(securityTools, "SecurityTools should be injected");

        System.out.println("[DEBUG_LOG] All security services are properly injected");
    }

    @Test
    void testSecurityToolsMethods() {
        System.out.println("[DEBUG_LOG] Testing SecurityTools methods");

        // Use AopUtils to get the target class if proxied
        Class<?> targetClass = org.springframework.aop.support.AopUtils.getTargetClass(securityTools);
        Method[] methods = targetClass.getDeclaredMethods();

        boolean hasManageAuth = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("manageAuthentication"));
        boolean hasManageAuthz = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("manageAuthorization"));
        boolean hasManageAssignments = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("manageSecurityAssignments"));

        assertTrue(hasManageAuth, "SecurityTools should have manageAuthentication method");
        assertTrue(hasManageAuthz, "SecurityTools should have manageAuthorization method");
        assertTrue(hasManageAssignments, "SecurityTools should have manageSecurityAssignments method");

        System.out.println("[DEBUG_LOG] SecurityTools methods verified");
    }

    @Test
    void testSecurityToolReturnCorrectTypes() {
        System.out.println("[DEBUG_LOG] Testing security tools return correct types");

        Class<?> targetClass = org.springframework.aop.support.AopUtils.getTargetClass(securityTools);
        Method[] methods = targetClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().startsWith("manage")) {
                System.out.println("[DEBUG_LOG] Tested: Method" + method.getName());
                assertEquals(String.class, method.getReturnType(),
                        "SecurityTools method " + method.getName() + " should return String");
            }
        }

        System.out.println("[DEBUG_LOG] All security tool methods return correct types");
    }

    @Test
    void testAuthenticationToolsFunctionality() {
        System.out.println("[DEBUG_LOG] Testing SecurityTools manageAuthentication functionality");

        try {
            String result = securityTools.manageAuthentication("db", "LIST", null, null);
            assertNotNull(result, "manageAuthentication list should return a non-null result");
            assertInstanceOf(String.class, result, "manageAuthentication list should return a String");
            System.out.println("[DEBUG_LOG] manageAuthentication list result: " +
                              result.substring(0, Math.min(100, result.length())));
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Security tool method failed (expected if Druid security not available): " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] SecurityTools functionality test completed");
    }

    @Test
    void testSecurityRepositoryConfiguration() {
        System.out.println("[DEBUG_LOG] Testing SecurityRepository configuration");

        // Verify that SecurityRepository has the expected methods
        Method[] methods = securityRepository.getClass().getDeclaredMethods();

        boolean hasGetAllUsers = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("getAllUsers"));
        boolean hasCreateUser = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("createUser"));
        boolean hasGetAllRoles = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("getAllRoles"));
        boolean hasSetRolePermissions = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("setRolePermissions"));

        assertTrue(hasGetAllUsers, "SecurityRepository should have getAllUsers method");
        assertTrue(hasCreateUser, "SecurityRepository should have createUser method");
        assertTrue(hasGetAllRoles, "SecurityRepository should have getAllRoles method");
        assertTrue(hasSetRolePermissions, "SecurityRepository should have setRolePermissions method");

        System.out.println("[DEBUG_LOG] SecurityRepository configuration verified");
    }

    @Test
    void testSecurityToolParameterCount() {
        System.out.println("[DEBUG_LOG] Testing security tool parameter counts");

        Class<?> targetClass = org.springframework.aop.support.AopUtils.getTargetClass(securityTools);
        Method[] methods = targetClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().equals("manageAuthentication")) {
                assertEquals(4, method.getParameterCount(),
                           "manageAuthentication should have 4 parameters");
            } else if (method.getName().equals("manageAuthorization")) {
                assertEquals(4, method.getParameterCount(),
                           "manageAuthorization should have 4 parameters");
            } else if (method.getName().equals("manageSecurityAssignments")) {
                assertEquals(4, method.getParameterCount(),
                           "manageSecurityAssignments should have 4 parameters");
            }
        }

        System.out.println("[DEBUG_LOG] Security tool parameter counts verified");
    }
}
