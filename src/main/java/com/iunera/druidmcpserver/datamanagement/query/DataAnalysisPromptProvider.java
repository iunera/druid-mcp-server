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
 * Data Analysis & Query Prompts for Druid MCP Server
 */
@Service
public class DataAnalysisPromptProvider {

    private final PromptTemplateService promptTemplateService;

    public DataAnalysisPromptProvider(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    /**
     * Explore and analyze data in Druid datasources
     */
    @McpPrompt(name = "druid-data-exploration", description = "Explore and analyze data in Druid datasources")
    public GetPromptResult dataExplorationPrompt(
            @McpArg(name = "datasource", description = "Name of the datasource to explore", required = false) String datasource) {

        Map<String, String> variables = promptTemplateService.createVariables();

        // Handle conditional datasource section
        String datasourceSection = "";
        if (datasource != null && !datasource.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "datasource", datasource);
            datasourceSection = promptTemplateService.loadSection("prompts.druid-data-exploration.datasource-section", sectionVars);
        }

        promptTemplateService.addVariable(variables, "datasource_section", datasourceSection);

        String template = promptTemplateService.loadTemplate("prompts.druid-data-exploration.template", variables);

        return new GetPromptResult("Druid Data Exploration",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }

    /**
     * Help optimize Druid SQL queries for better performance
     */
    @McpPrompt(name = "druid-query-optimization", description = "Help optimize Druid SQL queries for better performance")
    public GetPromptResult queryOptimizationPrompt(
            @McpArg(name = "query", description = "The SQL query to optimize", required = true) String query) {

        Map<String, String> variables = promptTemplateService.createVariables();
        promptTemplateService.addVariable(variables, "query", query);

        String template = promptTemplateService.loadTemplate("prompts.druid-query-optimization.template", variables);

        return new GetPromptResult("Druid Query Optimization",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }
}
