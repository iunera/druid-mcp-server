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

package com.iunera.druidmcpserver.ingestion.spec;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Component
public class IngestionSpecTools {

    private final IngestionSpecRepository ingestionSpecRepository;
    private final ObjectMapper objectMapper;

    public IngestionSpecTools(IngestionSpecRepository ingestionSpecRepository,
                              ObjectMapper objectMapper) {
        this.ingestionSpecRepository = ingestionSpecRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Submit an ingestion specification or generate a template
     */
    @McpTool(description = "Submit a Druid ingestion specification or generate a simple batch template. Parameters: [action] (Enum: SUBMIT_SPEC, GENERATE_TEMPLATE, required), [payloadJson] (String, optional) containing the ingestion spec for SUBMIT_SPEC, [datasourceName] (String, optional) for template generation, [inputSourceType] (String, optional) for template generation, and [inputSourcePath] (String, optional) for template generation.")
    public String submitIngestion(
            @McpToolParam(description = "Action: SUBMIT_SPEC, GENERATE_TEMPLATE (required)", required = true) String action,
            @McpToolParam(description = "Ingestion specification JSON string (required for SUBMIT_SPEC)", required = false) String payloadJson,
            @McpToolParam(description = "Name of the datasource (optional/required for GENERATE_TEMPLATE)", required = false) String datasourceName,
            @McpToolParam(description = "Input source type: local, s3, http (optional/required for GENERATE_TEMPLATE)", required = false) String inputSourceType,
            @McpToolParam(description = "Input source path/URI (optional/required for GENERATE_TEMPLATE)", required = false) String inputSourcePath
    ) {
        try {
            if (action == null) {
                return "Error: [action] parameter is required";
            }

            switch (action.toUpperCase()) {
                case "SUBMIT_SPEC":
                    if (payloadJson == null || payloadJson.trim().isEmpty()) {
                        return "Error: [payloadJson] containing the JSON spec is required for SUBMIT_SPEC action";
                    }
                    Map<String, Object> ingestionSpec = objectMapper.readValue(payloadJson, Map.class);
                    if (!ingestionSpec.containsKey("type")) {
                        return "Error: Ingestion spec must contain a 'type' field (e.g., 'index_parallel', 'index', 'kafka', etc.)";
                    }
                    if (!ingestionSpec.containsKey("spec")) {
                        return "Error: Ingestion spec must contain a 'spec' field with the ingestion specification";
                    }
                    JsonNode result = ingestionSpecRepository.submitIngestionSpec(ingestionSpec);
                    return objectMapper.writeValueAsString(result);

                case "GENERATE_TEMPLATE":
                    if (datasourceName == null || datasourceName.trim().isEmpty()) {
                        return "Error: [datasourceName] is required for GENERATE_TEMPLATE action";
                    }
                    if (inputSourceType == null || inputSourceType.trim().isEmpty()) {
                        return "Error: [inputSourceType] is required for GENERATE_TEMPLATE action";
                    }
                    if (inputSourcePath == null || inputSourcePath.trim().isEmpty()) {
                        return "Error: [inputSourcePath] is required for GENERATE_TEMPLATE action";
                    }

                    Map<String, Object> templateSpec = new HashMap<>();
                    templateSpec.put("type", "index_parallel");

                    Map<String, Object> spec = new HashMap<>();

                    // Data schema
                    Map<String, Object> dataSchema = new HashMap<>();
                    dataSchema.put("dataSource", datasourceName);
                    dataSchema.put("timestampSpec", Map.of(
                            "column", "__time",
                            "format", "auto"
                    ));
                    dataSchema.put("dimensionsSpec", Map.of(
                            "dimensions", new Object[0] // Auto-discover dimensions
                    ));
                    dataSchema.put("granularitySpec", Map.of(
                            "type", "uniform",
                            "segmentGranularity", "DAY",
                            "queryGranularity", "HOUR",
                            "rollup", false
                    ));

                    // IO config
                    Map<String, Object> ioConfig = new HashMap<>();
                    ioConfig.put("type", "index_parallel");

                    Map<String, Object> inputSource = new HashMap<>();
                    inputSource.put("type", inputSourceType);

                    switch (inputSourceType.toLowerCase()) {
                        case "local":
                            inputSource.put("baseDir", inputSourcePath);
                            inputSource.put("filter", "*.json");
                            break;
                        case "s3":
                            inputSource.put("uris", new String[]{inputSourcePath});
                            break;
                        case "http":
                            inputSource.put("uris", new String[]{inputSourcePath});
                            break;
                        default:
                            inputSource.put("uris", new String[]{inputSourcePath});
                    }

                    ioConfig.put("inputSource", inputSource);
                    ioConfig.put("inputFormat", Map.of("type", "json"));

                    // Tuning config
                    Map<String, Object> tuningConfig = new HashMap<>();
                    tuningConfig.put("type", "index_parallel");
                    tuningConfig.put("maxRowsPerSegment", 5000000);
                    tuningConfig.put("maxRowsInMemory", 1000000);

                    spec.put("dataSchema", dataSchema);
                    spec.put("ioConfig", ioConfig);
                    spec.put("tuningConfig", tuningConfig);

                    templateSpec.put("spec", spec);

                    return objectMapper.writeValueAsString(templateSpec);

                default:
                    return String.format("Error: Unsupported action '%s'. Supported: SUBMIT_SPEC, GENERATE_TEMPLATE", action);
            }
        } catch (RestClientException e) {
            return String.format("Error executing ingestion action '%s': %s", action, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process ingestion action '%s': %s", action, e.getMessage());
        }
    }
}