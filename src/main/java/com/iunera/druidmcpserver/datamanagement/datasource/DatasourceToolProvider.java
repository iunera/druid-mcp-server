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

package com.iunera.druidmcpserver.datamanagement.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DatasourceToolProvider {

    private final DatasourceRepository datasourceRepository;
    private final ObjectMapper objectMapper;

    public DatasourceToolProvider(DatasourceRepository datasourceRepository,
                                  ObjectMapper objectMapper) {
        this.datasourceRepository = datasourceRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * List all datasource names
     */
    @Tool(description = "List all available Druid datasource names")
    public String listDatasources() {
        try {
            JsonNode result = datasourceRepository.getAllDatasources();

            // Create a list to hold only datasource names
            List<String> datasourceNames = new ArrayList<>();

            for (int i = 0; i < result.size(); i++) {
                JsonNode datasource = result.get(i);
                String datasourceName = datasource.has("TABLE_NAME") ?
                        datasource.get("TABLE_NAME").asText() : "datasource_" + i;
                datasourceNames.add(datasourceName);
            }

            return objectMapper.writeValueAsString(datasourceNames);
        } catch (RestClientException e) {
            return String.format("Error listing datasources: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process datasources response: %s", e.getMessage());
        }
    }

    /**
     * Show detailed information for a specific datasource including column names and data types
     */
    @Tool(description = "Show detailed information for a specific Druid datasource including column information with names and data types")
    public String showDatasourceDetails(String datasourceName) {
        try {
            JsonNode result = datasourceRepository.getAllDatasources();

            // Find the specific datasource
            for (int i = 0; i < result.size(); i++) {
                JsonNode datasource = result.get(i);
                String currentDatasourceName = datasource.has("TABLE_NAME") ?
                        datasource.get("TABLE_NAME").asText() : "datasource_" + i;

                if (currentDatasourceName.equals(datasourceName)) {
                    // Get detailed information for this datasource
                    Map<String, Object> datasourceInfo = datasourceRepository.buildDatasourceInfo(datasource, datasourceName);
                    return objectMapper.writeValueAsString(datasourceInfo);
                }
            }

            return String.format("Datasource '%s' not found", datasourceName);
        } catch (RestClientException e) {
            return String.format("Error showing datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process datasource '%s' response: %s", datasourceName, e.getMessage());
        }
    }

    /**
     * Kill a datasource permanently with all its data and metadata
     */
    @Tool(description = "Kill a Druid datasource permanently, removing all data and metadata for the specified time interval. Use with extreme caution as this operation is irreversible.")
    public String killDatasource(String datasourceName, String interval) {
        try {
            JsonNode result = datasourceRepository.killDatasource(datasourceName, interval);
            if (result == null) {
                return String.format("Kill task for datasource '%s' was submitted successfully (no response body)", datasourceName);
            }
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error killing datasource '%s': %s", datasourceName, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process kill datasource '%s' response: %s", datasourceName, e.getMessage());
        }
    }
}
