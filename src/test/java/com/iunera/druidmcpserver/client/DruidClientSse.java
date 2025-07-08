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

import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;

/**
 * Druid MCP client using SSE/HTTP transport.
 * Requires the druid-mcp-server to be running on localhost:8080
 */
public class DruidClientSse {

    public static void main(String[] args) {
        var transport = HttpClientSseClientTransport.builder("http://localhost:8080").build();
        new DruidSampleClient(transport).run();
    }
}