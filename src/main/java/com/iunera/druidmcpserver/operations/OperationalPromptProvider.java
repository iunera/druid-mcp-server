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

package com.iunera.druidmcpserver.operations;

import com.iunera.druidmcpserver.config.PromptTemplateService;
import com.logaritex.mcp.annotation.McpPrompt;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Operational Prompts for Druid MCP Server
 */
@Service
public class OperationalPromptProvider {

    private final PromptTemplateService promptTemplateService;

    public OperationalPromptProvider(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    /**
     * Emergency response procedures for Druid cluster issues
     */
    @McpPrompt(name = "druid-emergency-response", description = "Emergency response procedures for Druid cluster issues")
    public GetPromptResult emergencyResponsePrompt() {

        Map<String, String> variables = promptTemplateService.createVariables();
        String template = promptTemplateService.loadTemplate("prompts.druid-emergency-response.template", variables);

        return new GetPromptResult("Druid Emergency Response",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }

    /**
     * Prepare cluster for maintenance operations
     */
    @McpPrompt(name = "druid-maintenance-mode", description = "Prepare cluster for maintenance operations")
    public GetPromptResult maintenanceModePrompt() {

        Map<String, String> variables = promptTemplateService.createVariables();
        String template = promptTemplateService.loadTemplate("prompts.druid-maintenance-mode.template", variables);

        return new GetPromptResult("Druid Maintenance Mode",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }
}
