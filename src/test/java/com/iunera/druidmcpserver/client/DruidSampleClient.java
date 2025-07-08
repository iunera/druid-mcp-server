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

package com.iunera.druidmcpserver.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

import java.util.Map;

/**
 * Sample client for testing Druid MCP server functionality
 */
public class DruidSampleClient {

    private final McpClientTransport transport;

    public DruidSampleClient(McpClientTransport transport) {
        this.transport = transport;
    }

    public void run() {

        var client = McpClient.sync(this.transport).build();

        client.initialize();

        client.ping();

        // List and demonstrate tools
        ListToolsResult toolsList = client.listTools();
        System.out.println("Available Druid Tools = " + toolsList);
        toolsList.tools().stream().forEach(tool -> {
            System.out.println("Tool: " + tool.name() + ", description: " + tool.description() + ", schema: " + tool.inputSchema());
        });

        // Test listDatasources tool
        System.out.println("\n=== Testing listDatasources ===");
        CallToolResult datasourcesResult = client.callTool(new CallToolRequest("listDatasources", Map.of()));
        System.out.println("Datasources Result: " + datasourcesResult);

        // Test listLookups tool
        System.out.println("\n=== Testing listLookups ===");
        CallToolResult lookupsResult = client.callTool(new CallToolRequest("listLookups", Map.of()));
        System.out.println("Lookups Result: " + lookupsResult);

        // Test queryDruidSql tool with a simple query
        System.out.println("\n=== Testing queryDruidSql ===");
        String testQuery = "SELECT * FROM \"INFORMATION_SCHEMA\".\"TABLES\" WHERE \"TABLE_SCHEMA\" = 'druid' LIMIT 5";
        CallToolResult queryResult = client.callTool(new CallToolRequest("queryDruidSql",
                Map.of("sqlQuery", testQuery)));
        System.out.println("Query Result: " + queryResult);

        // Test queryDruidSql with a more complex query (if datasources exist)
        System.out.println("\n=== Testing queryDruidSql with system query ===");
        String systemQuery = "SELECT * FROM sys.segments LIMIT 3";
        CallToolResult systemQueryResult = client.callTool(new CallToolRequest("queryDruidSql",
                Map.of("sqlQuery", systemQuery)));
        System.out.println("System Query Result: " + systemQueryResult);

        client.closeGracefully();
    }
}