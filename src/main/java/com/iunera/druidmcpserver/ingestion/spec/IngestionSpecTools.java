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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
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
     * Create and submit a Druid ingestion spec
     */
    @McpTool(description = "Create and submit a Druid ingestion specification. Provide the ingestion spec as a JSON string containing the task type, spec, and context.")
    public String createIngestionSpec(String ingestionSpecJson) {
        try {
            // Parse the ingestion spec JSON
            Map<String, Object> ingestionSpec = objectMapper.readValue(ingestionSpecJson, Map.class);

            // Validate required fields
            if (!ingestionSpec.containsKey("type")) {
                return "Error: Ingestion spec must contain a 'type' field (e.g., 'index_parallel', 'index', 'kafka', etc.)";
            }

            if (!ingestionSpec.containsKey("spec")) {
                return "Error: Ingestion spec must contain a 'spec' field with the ingestion specification";
            }

            // Submit the ingestion spec
            JsonNode result = ingestionSpecRepository.submitIngestionSpec(ingestionSpec);
            return objectMapper.writeValueAsString(result);

        } catch (RestClientException e) {
            return String.format("Error submitting ingestion spec: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process ingestion spec: %s", e.getMessage());
        }
    }

    /**
     * Create a simple batch ingestion spec template
     */
    @McpTool(description = "Create a template for a simple batch ingestion spec. Provide datasource name, input source type (local, s3, http, etc.), and input source path.")
    public String createBatchIngestionTemplate(String datasourceName, String inputSourceType, String inputSourcePath) {
        try {
            Map<String, Object> ingestionSpec = new HashMap<>();
            ingestionSpec.put("type", "index_parallel");

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

            ingestionSpec.put("spec", spec);

            return objectMapper.writeValueAsString(ingestionSpec);

        } catch (Exception e) {
            return String.format("Failed to create batch ingestion template: %s", e.getMessage());
        }
    }


}