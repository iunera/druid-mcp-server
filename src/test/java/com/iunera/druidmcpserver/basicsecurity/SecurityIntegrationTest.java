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
    private AuthenticationTools authenticationTools;

    @Autowired
    private AuthorizationTools authorizationTools;

    @Test
    void testSecurityServicesAreInjected() {
        System.out.println("[DEBUG_LOG] Testing security services injection");

        assertNotNull(securityRepository, "SecurityRepository should be injected");
        assertNotNull(authenticationTools, "AuthenticationTools should be injected");
        assertNotNull(authorizationTools, "AuthorizationTools should be injected");

        System.out.println("[DEBUG_LOG] All security services are properly injected");
    }

    @Test
    void testAuthenticationToolProviderMethods() {
        System.out.println("[DEBUG_LOG] Testing AuthenticationTools methods");

        // Check that key methods exist
        Method[] methods = authenticationTools.getClass().getDeclaredMethods();
        boolean hasListUsers = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("listAuthenticationUsers"));
        boolean hasCreateUser = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("createAuthenticationUser"));
        boolean hasSetPassword = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("setUserPassword"));

        assertTrue(hasListUsers, "AuthenticationTools should have listAuthenticationUsers method");
        assertTrue(hasCreateUser, "AuthenticationTools should have createAuthenticationUser method");
        assertTrue(hasSetPassword, "AuthenticationTools should have setUserPassword method");

        System.out.println("[DEBUG_LOG] AuthenticationTools methods verified");
    }

    @Test
    void testAuthorizationToolProviderMethods() {
        System.out.println("[DEBUG_LOG] Testing AuthorizationTools methods");

        // Check that key methods exist
        Method[] methods = authorizationTools.getClass().getDeclaredMethods();
        boolean hasListRoles = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("listRoles"));
        boolean hasCreateRole = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("createRole"));
        boolean hasSetPermissions = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("setRolePermissions"));
        boolean hasAssignRole = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("assignRoleToUser"));

        assertTrue(hasListRoles, "AuthorizationTools should have listRoles method");
        assertTrue(hasCreateRole, "AuthorizationTools should have createRole method");
        assertTrue(hasSetPermissions, "AuthorizationTools should have setRolePermissions method");
        assertTrue(hasAssignRole, "AuthorizationTools should have assignRoleToUser method");

        System.out.println("[DEBUG_LOG] AuthorizationTools methods verified");
    }

    @Test
    void testSecurityToolReturnCorrectTypes() {
        System.out.println("[DEBUG_LOG] Testing security tools return correct types");

        // Test that methods return String (as required by MCP tools)
        Method[] authMethods = authenticationTools.getClass().getDeclaredMethods();
        Method[] authzMethods = authorizationTools.getClass().getDeclaredMethods();

        // Check authentication methods
        for (Method method : authMethods) {
            if (method.getName().startsWith("list") || method.getName().startsWith("get") || 
                method.getName().startsWith("create") || method.getName().startsWith("delete") || 
                method.getName().startsWith("set")) {
                assertEquals(String.class, method.getReturnType(),
                        "AuthenticationTools method " + method.getName() + " should return String");
            }
        }

        // Check authorization methods
        for (Method method : authzMethods) {
            if (method.getName().startsWith("list") || method.getName().startsWith("get") || 
                method.getName().startsWith("create") || method.getName().startsWith("delete") || 
                method.getName().startsWith("set") || method.getName().startsWith("assign") ||
                method.getName().startsWith("unassign")) {
                assertEquals(String.class, method.getReturnType(),
                        "AuthorizationTools method " + method.getName() + " should return String");
            }
        }

        System.out.println("[DEBUG_LOG] All security tool methods return correct types");
    }

    @Test
    void testAuthenticationToolProviderFunctionality() {
        System.out.println("[DEBUG_LOG] Testing AuthenticationTools basic functionality");

        // Test that we can call the methods without exceptions
        // Note: These will return error messages if Druid is not available,
        // or successful responses if Druid is running with security enabled

        try {
            String result = authenticationTools.listAuthenticationUsers("db");
            assertNotNull(result, "listAuthenticationUsers should return a non-null result");
            assertInstanceOf(String.class, result, "listAuthenticationUsers should return a String");
            System.out.println("[DEBUG_LOG] listAuthenticationUsers result: " + 
                              result.substring(0, Math.min(100, result.length())));
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Authentication tool method failed (expected if Druid security not available): " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] AuthenticationTools functionality test completed");
    }

    @Test
    void testAuthorizationToolProviderFunctionality() {
        System.out.println("[DEBUG_LOG] Testing AuthorizationTools basic functionality");

        try {
            String result = authorizationTools.listRoles("db");
            assertNotNull(result, "listRoles should return a non-null result");
            assertInstanceOf(String.class, result, "listRoles should return a String");
            System.out.println("[DEBUG_LOG] listRoles result: " + 
                              result.substring(0, Math.min(100, result.length())));
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Authorization tool method failed (expected if Druid security not available): " + e.getMessage());
        }

        System.out.println("[DEBUG_LOG] AuthorizationTools functionality test completed");
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

        // Test that methods have the expected number of parameters
        Method[] authMethods = authenticationTools.getClass().getDeclaredMethods();
        
        for (Method method : authMethods) {
            if (method.getName().equals("listAuthenticationUsers")) {
                assertEquals(1, method.getParameterCount(), 
                           "listAuthenticationUsers should have 1 parameter (authenticatorName)");
            } else if (method.getName().equals("createAuthenticationUser")) {
                assertEquals(2, method.getParameterCount(), 
                           "createAuthenticationUser should have 2 parameters (authenticatorName, userName)");
            } else if (method.getName().equals("setUserPassword")) {
                assertEquals(3, method.getParameterCount(), 
                           "setUserPassword should have 3 parameters (authenticatorName, userName, password)");
            }
        }

        Method[] authzMethods = authorizationTools.getClass().getDeclaredMethods();
        
        for (Method method : authzMethods) {
            if (method.getName().equals("assignRoleToUser")) {
                assertEquals(3, method.getParameterCount(), 
                           "assignRoleToUser should have 3 parameters (authorizerName, userName, roleName)");
            } else if (method.getName().equals("setRolePermissions")) {
                assertEquals(3, method.getParameterCount(), 
                           "setRolePermissions should have 3 parameters (authorizerName, roleName, permissions)");
            }
        }

        System.out.println("[DEBUG_LOG] Security tool parameter counts verified");
    }
}