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

package com.iunera.druidmcpserver.datamanagement.retention;

import com.iunera.druidmcpserver.config.PromptTemplateService;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Data Retention Management Prompts for Druid MCP Server
 */
@Service
public class RetentionPromptProvider {

    private final PromptTemplateService promptTemplateService;

    public RetentionPromptProvider(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    /**
     * Manage data retention policies across datasources
     */
    @McpPrompt(name = "druid-retention-management", description = "Manage data retention policies across datasources")
    public GetPromptResult retentionManagementPrompt(
            @McpArg(name = "datasource", description = "Specific datasource to manage", required = false) String datasource) {

        Map<String, String> variables = promptTemplateService.createVariables();

        // Handle conditional datasource section
        String datasourceSection;
        if (datasource != null && !datasource.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "datasource", datasource);
            datasourceSection = promptTemplateService.loadSection("prompts.druid-retention-management.datasource-section-specific", sectionVars);
        } else {
            datasourceSection = promptTemplateService.loadSection("prompts.druid-retention-management.datasource-section-all", null);
        }

        promptTemplateService.addVariable(variables, "datasource_section", datasourceSection);

        String template = promptTemplateService.loadTemplate("prompts.druid-retention-management.template", variables);

        return new GetPromptResult("Druid Retention Management",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }
}