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
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
@Component
public class WriteDatasourceTools {

    private final DatasourceRepository datasourceRepository;
    private final ObjectMapper objectMapper;

    public WriteDatasourceTools(DatasourceRepository datasourceRepository,
                                ObjectMapper objectMapper) {
        this.datasourceRepository = datasourceRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Kill a datasource permanently with all its data and metadata
     */
    @McpTool(description = "Kill a Druid datasource permanently, removing all data and metadata for the specified time interval. Use with extreme caution as this operation is irreversible.")
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
