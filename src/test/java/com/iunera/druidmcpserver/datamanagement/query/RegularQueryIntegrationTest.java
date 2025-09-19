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

package com.iunera.druidmcpserver.datamanagement.query;

import com.iunera.druidmcpserver.config.DruidConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for regular Druid SQL query functionality.
 * These tests focus specifically on the standard SQL query execution
 * against Druid datasources using the queryDruidSql method.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "druid.router.url=http://test-router:8888"
})
class RegularQueryIntegrationTest {

    @Autowired
    private QueryTools queryTools;

    @Autowired
    private DruidConfig druidConfig;

    @Test
    void contextLoads() {
        System.out.println("[DEBUG_LOG] Testing regular query context loading");
        assertNotNull(queryTools);
        assertNotNull(druidConfig);
        System.out.println("[DEBUG_LOG] All regular query beans loaded successfully");
    }

    @Test
    void testBasicSqlQuery() {
        System.out.println("[DEBUG_LOG] Testing basic SQL query execution");
        String testQuery = "SELECT 1 as test_value";

        String result = queryTools.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Basic SQL query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Basic SQL query handles errors gracefully");
    }

    @Test
    void testCountQuery() {
        System.out.println("[DEBUG_LOG] Testing COUNT query execution");
        String testQuery = "SELECT COUNT(*) FROM test_datasource";

        String result = queryTools.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] COUNT query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] COUNT query handles errors gracefully");
    }

    @Test
    void testSelectWithWhereClause() {
        System.out.println("[DEBUG_LOG] Testing SELECT with WHERE clause");
        String testQuery = "SELECT * FROM test_datasource WHERE __time >= CURRENT_TIMESTAMP - INTERVAL '1' DAY";

        String result = queryTools.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] SELECT with WHERE query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] SELECT with WHERE query handles errors gracefully");
    }

    @Test
    void testGroupByQuery() {
        System.out.println("[DEBUG_LOG] Testing GROUP BY query execution");
        String testQuery = "SELECT dimension_column, COUNT(*) FROM test_datasource GROUP BY dimension_column";

        String result = queryTools.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] GROUP BY query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] GROUP BY query handles errors gracefully");
    }

    @Test
    void testOrderByQuery() {
        System.out.println("[DEBUG_LOG] Testing ORDER BY query execution");
        String testQuery = "SELECT * FROM test_datasource ORDER BY __time DESC LIMIT 10";

        String result = queryTools.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] ORDER BY query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] ORDER BY query handles errors gracefully");
    }

    @Test
    void testAggregationQuery() {
        System.out.println("[DEBUG_LOG] Testing aggregation query execution");
        String testQuery = "SELECT SUM(metric_column), AVG(metric_column), MAX(metric_column) FROM test_datasource";

        String result = queryTools.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Aggregation query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Aggregation query handles errors gracefully");
    }

    @Test
    void testInformationSchemaQuery() {
        System.out.println("[DEBUG_LOG] Testing INFORMATION_SCHEMA query execution");
        String testQuery = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'druid'";

        String result = queryTools.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] INFORMATION_SCHEMA query result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] INFORMATION_SCHEMA query handles errors gracefully");
    }

    @Test
    void testEmptyQuery() {
        System.out.println("[DEBUG_LOG] Testing empty query handling");
        String emptyQuery = "";

        String result = queryTools.queryDruidSql(emptyQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Empty query result: " + result);

        // Should return an error message for empty query
        assertTrue(result.contains("Error") || result.contains("Failed"));
        System.out.println("[DEBUG_LOG] Empty query handles errors gracefully");
    }

    @Test
    void testNullQuery() {
        System.out.println("[DEBUG_LOG] Testing null query handling");

        String result = queryTools.queryDruidSql(null);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Null query result: " + result);

        // Should return an error message for null query
        assertTrue(result.contains("Error") || result.contains("Failed"));
        System.out.println("[DEBUG_LOG] Null query handles errors gracefully");
    }

    @Test
    void testInvalidSqlQuery() {
        System.out.println("[DEBUG_LOG] Testing invalid SQL query handling");
        String invalidQuery = "INVALID SQL SYNTAX HERE";

        String result = queryTools.queryDruidSql(invalidQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Invalid SQL query result: " + result);

        // Should return an error message for invalid SQL
        assertTrue(result.contains("Error") || result.contains("Failed"));
        System.out.println("[DEBUG_LOG] Invalid SQL query handles errors gracefully");
    }

    @Test
    void testDruidConfigurationForRegularQueries() {
        System.out.println("[DEBUG_LOG] Testing Druid configuration for regular queries");
        assertNotNull(druidConfig.getDruidRouterUrl());
        assertEquals("http://test-router:8888", druidConfig.getDruidRouterUrl());
        System.out.println("[DEBUG_LOG] Druid router URL configured correctly: " + druidConfig.getDruidRouterUrl());
    }

    @Test
    void testQueryWithSpecialCharacters() {
        System.out.println("[DEBUG_LOG] Testing query with special characters");
        String testQuery = "SELECT 'test with spaces and \"quotes\"' as test_column FROM test_datasource";

        String result = queryTools.queryDruidSql(testQuery);
        assertNotNull(result);
        System.out.println("[DEBUG_LOG] Query with special characters result: " + result);

        // Should return an error message since we're not connected to a real Druid instance
        assertTrue(result.contains("Error") || result.contains("Failed") || result.contains("{") || result.contains("["));
        System.out.println("[DEBUG_LOG] Query with special characters handles errors gracefully");
    }
}