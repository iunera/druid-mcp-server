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
 * Unit tests verifying that protected roles (e.g., admin, druid_system)
 * cannot be modified via SecurityRepository methods.
 */
class SecurityRepositoryGuardTest {

    @Test
    void deleteRole_adminRoleShouldThrow() {
        System.out.println("[DEBUG_LOG] Verifying deleteRole rejects 'admin' role");
        SecurityRepository repo = new SecurityRepository(null);
        assertThrows(IllegalArgumentException.class, () ->
                repo.deleteRole("authorizer", "admin"),
                "Deleting 'admin' role must be forbidden");
    }

    @Test
    void deleteRole_adminRoleCaseInsensitiveAndTrimShouldThrow() {
        System.out.println("[DEBUG_LOG] Verifying deleteRole rejects ' Admin ' role (case-insensitive, trimmed)");
        SecurityRepository repo = new SecurityRepository(null);
        assertThrows(IllegalArgumentException.class, () ->
                repo.deleteRole("authorizer", " Admin "),
                "Deleting 'admin' role (with spaces/mixed case) must be forbidden");
    }

    @Test
    void setRolePermissions_druidSystemRoleShouldThrow() {
        System.out.println("[DEBUG_LOG] Verifying setRolePermissions rejects 'druid_system' role");
        SecurityRepository repo = new SecurityRepository(null);
        assertThrows(IllegalArgumentException.class, () ->
                repo.setRolePermissions("authorizer", "druid_system", "[]"),
                "Modifying 'druid_system' role permissions must be forbidden");
    }
}
