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

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for user immutability rules in basic authentication.
 */
class SecurityRepositoryUserGuardTest {

    @Test
    void deleteUser_adminShouldThrow() {
        System.out.println("[DEBUG_LOG] Verifying deleteUser rejects 'admin' user");
        SecurityRepository repo = new SecurityRepository(null);
        assertThrows(IllegalArgumentException.class, () ->
                repo.deleteUser("authenticator", "admin"),
                "Deleting 'admin' user must be forbidden");
    }

    @Test
    void deleteUser_druidSystemShouldThrow() {
        System.out.println("[DEBUG_LOG] Verifying deleteUser rejects 'druid_system' user");
        SecurityRepository repo = new SecurityRepository(null);
        assertThrows(IllegalArgumentException.class, () ->
                repo.deleteUser("authenticator", "druid_system"),
                "Deleting 'druid_system' user must be forbidden");
    }

    @Test
    void setUserCredentials_druidSystemShouldThrow() {
        System.out.println("[DEBUG_LOG] Verifying setUserCredentials rejects 'druid_system' user");
        SecurityRepository repo = new SecurityRepository(null);
        assertThrows(IllegalArgumentException.class, () ->
                repo.setUserCredentials("authenticator", "druid_system", "secret"),
                "Changing 'druid_system' password must be forbidden");
    }
}
