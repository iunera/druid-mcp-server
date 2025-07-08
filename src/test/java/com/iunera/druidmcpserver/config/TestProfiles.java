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

package com.iunera.druidmcpserver.config;

/**
 * Constants for test profiles used in the Druid MCP Server tests.
 * 
 * This class defines the available test profiles that can be used to control
 * how tests interact with Druid services.
 */
public final class TestProfiles {
    
    /**
     * Profile for running tests with mock Druid services.
     * Use this profile when you want to run tests without a real Druid cluster.
     * 
     * Usage: @ActiveProfiles(TestProfiles.MOCK_DRUID)
     */
    public static final String MOCK_DRUID = "mock-druid";
    
    /**
     * Profile for running tests with real Druid services.
     * Use this profile when you have a Druid cluster running and want to test
     * against real Druid endpoints.
     * 
     * Usage: @ActiveProfiles(TestProfiles.INTEGRATION_DRUID)
     */
    public static final String INTEGRATION_DRUID = "integration-druid";
    
    private TestProfiles() {
        // Utility class - prevent instantiation
    }
}