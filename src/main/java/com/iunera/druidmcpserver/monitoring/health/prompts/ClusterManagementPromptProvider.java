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

package com.iunera.druidmcpserver.monitoring.health.prompts;

import com.iunera.druidmcpserver.config.PromptTemplateService;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Cluster Management Prompts for Druid MCP Server
 */
@Service
public class ClusterManagementPromptProvider {

    private final PromptTemplateService promptTemplateService;

    public ClusterManagementPromptProvider(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    /**
     * Comprehensive health assessment of the Druid cluster
     */
    @McpPrompt(name = "druid-health-check", description = "Comprehensive health assessment of the Druid cluster")
    public GetPromptResult healthCheckPrompt() {

        Map<String, String> variables = promptTemplateService.createVariables();
        String template = promptTemplateService.loadTemplate("prompts.druid-health-check.template", variables);

        return new GetPromptResult("Druid Health Check",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }

    /**
     * Get a complete overview of the Druid cluster status
     */
    @McpPrompt(name = "druid-cluster-overview", description = "Get a complete overview of the Druid cluster status")
    public GetPromptResult clusterOverviewPrompt() {

        Map<String, String> variables = promptTemplateService.createVariables();
        String template = promptTemplateService.loadTemplate("prompts.druid-cluster-overview.template", variables);

        return new GetPromptResult("Druid Cluster Overview",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }
}