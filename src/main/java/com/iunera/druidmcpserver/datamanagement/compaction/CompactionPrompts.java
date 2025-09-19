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

package com.iunera.druidmcpserver.datamanagement.compaction;

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
 * Compaction Suggestion Prompts for Druid MCP Server
 * Based on Apache Druid compaction best practices and recommendations
 */
@Service
public class CompactionPrompts {

    private final PromptTemplateService promptTemplateService;

    public CompactionPrompts(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    /**
     * Comprehensive compaction suggestions based on Apache Druid recommendations
     */
    @McpPrompt(name = "druid-compaction-suggestions", description = "Comprehensive compaction configuration suggestions based on Apache Druid best practices")
    public GetPromptResult compactionSuggestionsPrompt(
            @McpArg(name = "datasource", description = "Specific datasource to analyze for compaction", required = false) String datasource,
            @McpArg(name = "query_patterns", description = "Description of typical query patterns (e.g., 'time-series aggregations', 'high-cardinality groupBy')", required = false) String queryPatterns,
            @McpArg(name = "data_volume", description = "Daily data volume (e.g., 'low', 'medium', 'high', or specific GB/day)", required = false) String dataVolume) {

        Map<String, String> variables = promptTemplateService.createVariables();

        // Build context section with conditional parts
        StringBuilder contextSection = new StringBuilder();

        if (datasource != null && !datasource.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "datasource", datasource);
            contextSection.append(promptTemplateService.loadSection("prompts.druid-compaction-suggestions.context-datasource", sectionVars));
        } else {
            contextSection.append(promptTemplateService.loadSection("prompts.druid-compaction-suggestions.context-all-datasources", null));
        }

        if (queryPatterns != null && !queryPatterns.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "queryPatterns", queryPatterns);
            contextSection.append(promptTemplateService.loadSection("prompts.druid-compaction-suggestions.context-query-patterns", sectionVars));
        }

        if (dataVolume != null && !dataVolume.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "dataVolume", dataVolume);
            contextSection.append(promptTemplateService.loadSection("prompts.druid-compaction-suggestions.context-data-volume", sectionVars));
        }

        promptTemplateService.addVariable(variables, "context_section", contextSection.toString());

        String template = promptTemplateService.loadTemplate("prompts.druid-compaction-suggestions.template", variables);

        return new GetPromptResult("Apache Druid Compaction Suggestions",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }

    /**
     * Compaction troubleshooting and optimization prompt
     */
    @McpPrompt(name = "druid-compaction-troubleshooting", description = "Troubleshoot compaction issues and optimize performance")
    public GetPromptResult compactionTroubleshootingPrompt(
            @McpArg(name = "issue_description", description = "Description of the compaction issue or performance problem", required = false) String issueDescription,
            @McpArg(name = "datasource", description = "Affected datasource name", required = false) String datasource) {

        Map<String, String> variables = promptTemplateService.createVariables();

        // Build context section with conditional parts
        StringBuilder contextSection = new StringBuilder();

        if (issueDescription != null && !issueDescription.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "issueDescription", issueDescription);
            contextSection.append(promptTemplateService.loadSection("prompts.druid-compaction-troubleshooting.context-issue", sectionVars));
        }

        if (datasource != null && !datasource.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "datasource", datasource);
            contextSection.append(promptTemplateService.loadSection("prompts.druid-compaction-troubleshooting.context-datasource", sectionVars));
        }

        promptTemplateService.addVariable(variables, "context_section", contextSection.toString());

        String template = promptTemplateService.loadTemplate("prompts.druid-compaction-troubleshooting.template", variables);

        return new GetPromptResult("Druid Compaction Troubleshooting",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }

    /**
     * Optimize segment compaction for better performance
     */
    @McpPrompt(name = "druid-compaction-optimization", description = "Optimize segment compaction for better performance")
    public GetPromptResult compactionOptimizationPrompt(
            @McpArg(name = "datasource", description = "Datasource to optimize compaction for", required = false) String datasource) {

        Map<String, String> variables = promptTemplateService.createVariables();

        // Handle conditional datasource section
        String datasourceSection;
        if (datasource != null && !datasource.isEmpty()) {
            Map<String, String> sectionVars = promptTemplateService.createVariables();
            promptTemplateService.addVariable(sectionVars, "datasource", datasource);
            datasourceSection = promptTemplateService.loadSection("prompts.druid-compaction-optimization.datasource-section-specific", sectionVars);
        } else {
            datasourceSection = promptTemplateService.loadSection("prompts.druid-compaction-optimization.datasource-section-all", null);
        }

        promptTemplateService.addVariable(variables, "datasource_section", datasourceSection);

        String template = promptTemplateService.loadTemplate("prompts.druid-compaction-optimization.template", variables);

        return new GetPromptResult("Druid Compaction Optimization",
                List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }
}
