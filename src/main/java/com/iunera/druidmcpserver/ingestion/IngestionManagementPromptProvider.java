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

package com.iunera.druidmcpserver.ingestion;

import com.iunera.druidmcpserver.config.PromptTemplateService;
import com.logaritex.mcp.annotation.McpArg;
import com.logaritex.mcp.annotation.McpPrompt;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Data Ingestion Management Prompts for Druid MCP Server
 */
@Service
public class IngestionManagementPromptProvider {

    private final PromptTemplateService promptTemplateService;

    public IngestionManagementPromptProvider(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    /**
     * Troubleshoot data ingestion issues
     */
    @McpPrompt(name = "druid-ingestion-troubleshooting", description = "Troubleshoot data ingestion issues")
    public GetPromptResult ingestionTroubleshootingPrompt(
            @McpArg(name = "supervisor_id", description = "Specific supervisor ID to troubleshoot", required = false) String supervisorId) {

        Map<String, String> variables = promptTemplateService.createVariables();

        // Handle conditional supervisor section
        String supervisorSection = "";
        if (supervisorId != null && !supervisorId.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "supervisorId", supervisorId);
            supervisorSection = promptTemplateService.loadSection("prompts.druid-ingestion-troubleshooting.supervisor-section", sectionVars);
        }

        promptTemplateService.addVariable(variables, "supervisor_section", supervisorSection);

        String template = promptTemplateService.loadTemplate("prompts.druid-ingestion-troubleshooting.template", variables);

        return new GetPromptResult("Druid Ingestion Troubleshooting",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }

    /**
     * Guide for setting up new data ingestion
     */
    @McpPrompt(name = "druid-ingestion-setup", description = "Guide for setting up new data ingestion")
    public GetPromptResult ingestionSetupPrompt(
            @McpArg(name = "data_source", description = "Type of data source (kafka, kinesis, file, etc.)", required = false) String dataSource,
            @McpArg(name = "datasource_name", description = "Name for the new datasource", required = false) String datasourceName) {

        Map<String, String> variables = promptTemplateService.createVariables();

        // Handle conditional data source section
        String dataSourceSection;
        if (dataSource != null && !dataSource.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "dataSource", dataSource);
            dataSourceSection = promptTemplateService.loadSection("prompts.druid-ingestion-setup.data-source-section-specific", sectionVars);
        } else {
            dataSourceSection = promptTemplateService.loadSection("prompts.druid-ingestion-setup.data-source-section-help", null);
        }

        // Handle conditional datasource name section
        String datasourceNameSection;
        if (datasourceName != null && !datasourceName.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "datasourceName", datasourceName);
            datasourceNameSection = promptTemplateService.loadSection("prompts.druid-ingestion-setup.datasource-name-section-specific", sectionVars);
        } else {
            datasourceNameSection = promptTemplateService.loadSection("prompts.druid-ingestion-setup.datasource-name-section-suggest", null);
        }

        promptTemplateService.addVariable(variables, "data_source_section", dataSourceSection);
        promptTemplateService.addVariable(variables, "datasource_name_section", datasourceNameSection);

        String template = promptTemplateService.loadTemplate("prompts.druid-ingestion-setup.template", variables);

        return new GetPromptResult("Druid Ingestion Setup",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }
}
