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
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class DatasourceResources {

    private final DatasourceRepository datasourceRepository;
    private final ObjectMapper objectMapper;

    public DatasourceResources(DatasourceRepository datasourceRepository,
                               ObjectMapper objectMapper) {
        this.datasourceRepository = datasourceRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get basic information for a specific datasource
     */
    @McpResource(uri = "datasource://{datasourcename}", name = "Datasource", description = "Provides basic information for a specific Druid datasource")
    public ReadResourceResult getDatasource(ReadResourceRequest request, String datasourcename) {
        try {
            JsonNode allDatasources = datasourceRepository.getAllDatasources();

            // Find the specific datasource
            JsonNode targetDatasource = null;
            for (int i = 0; i < allDatasources.size(); i++) {
                JsonNode datasource = allDatasources.get(i);
                String datasourceName = datasource.has("TABLE_NAME") ?
                        datasource.get("TABLE_NAME").asText() : "datasource_" + i;

                if (datasourceName.equals(datasourcename)) {
                    targetDatasource = datasource;
                    break;
                }
            }

            if (targetDatasource == null) {
                String errorMessage = String.format("Datasource '%s' not found", datasourcename);
                return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
            }

            String datasourceJson = objectMapper.writeValueAsString(objectMapper.convertValue(targetDatasource, Map.class));
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "application/json", datasourceJson)));

        } catch (RestClientException e) {
            String errorMessage = String.format("Error retrieving datasource '%s': %s", datasourcename, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to process datasource '%s': %s", datasourcename, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", errorMessage)));
        }
    }

    /**
     * Get detailed information for a specific datasource including column information
     */
    @McpResource(uri = "datasource-details://{datasourcename}", name = "Datasource Details", description = "Provides detailed information for a specific Druid datasource including column information")
    public ReadResourceResult getDatasourceDetails(String datasourcename) {
        try {
            JsonNode allDatasources = datasourceRepository.getAllDatasources();

            // Find the specific datasource
            JsonNode targetDatasource = null;
            for (int i = 0; i < allDatasources.size(); i++) {
                JsonNode datasource = allDatasources.get(i);
                String datasourceName = datasource.has("TABLE_NAME") ?
                        datasource.get("TABLE_NAME").asText() : "datasource_" + i;

                if (datasourceName.equals(datasourcename)) {
                    targetDatasource = datasource;
                    break;
                }
            }

            if (targetDatasource == null) {
                String errorMessage = String.format("Datasource '%s' not found", datasourcename);
                return new ReadResourceResult(List.of(new TextResourceContents("datasource-details://" + datasourcename, "text/plain", errorMessage)));
            }

            // Get detailed information for this datasource
            Map<String, Object> datasourceInfo = datasourceRepository.buildDatasourceInfo(targetDatasource, datasourcename);
            String datasourceJson = objectMapper.writeValueAsString(datasourceInfo);

            return new ReadResourceResult(List.of(new TextResourceContents("datasource-details://" + datasourcename, "application/json", datasourceJson)));

        } catch (RestClientException e) {
            String errorMessage = String.format("Error retrieving datasource details for '%s': %s", datasourcename, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents("datasource-details://" + datasourcename, "text/plain", errorMessage)));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to process datasource details for '%s': %s", datasourcename, e.getMessage());
            return new ReadResourceResult(List.of(new TextResourceContents("datasource-details://" + datasourcename, "text/plain", errorMessage)));
        }
    }
}
