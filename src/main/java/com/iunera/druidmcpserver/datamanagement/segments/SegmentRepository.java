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

package com.iunera.druidmcpserver.datamanagement.segments;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Repository
public class SegmentRepository {

    private final RestClient druidRouterRestClient;

    public SegmentRepository(@Qualifier("druidRouterRestClient") RestClient druidRouterRestClient) {
        this.druidRouterRestClient = druidRouterRestClient;

    }

    /**
     * Get all segments for all datasources
     */
    public JsonNode getAllSegments() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/datasources?full")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get segments for a specific datasource
     */
    public JsonNode getSegmentsForDatasource(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/datasources/{datasourceName}/segments", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get segments for a specific datasource with full details
     */
    public JsonNode getSegmentsForDatasourceWithDetails(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/datasources/{datasourceName}/segments?full", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get specific segment details
     */
    public JsonNode getSegmentDetails(String datasourceName, String segmentId) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/datasources/{datasourceName}/segments/{segmentId}", datasourceName, segmentId)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get unused segments for a datasource
     */
    public JsonNode getUnusedSegments(String datasourceName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/datasources/{datasourceName}/segments?full", datasourceName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Enable a segment
     */
    public JsonNode enableSegment(String datasourceName, String segmentId) throws RestClientException {
        return druidRouterRestClient
                .post()
                .uri("/druid/coordinator/v1/datasources/{datasourceName}/segments/{segmentId}", datasourceName, segmentId)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Disable a segment
     */
    public JsonNode disableSegment(String datasourceName, String segmentId) throws RestClientException {
        return druidRouterRestClient
                .delete()
                .uri("/druid/coordinator/v1/datasources/{datasourceName}/segments/{segmentId}", datasourceName, segmentId)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get segment metadata using SQL query
     */
    public JsonNode getSegmentMetadata() throws RestClientException {
        String sql = "SELECT * FROM sys.segments ORDER BY datasource, \"start\", \"end\" LIMIT 100";

        Map<String, Object> query = new HashMap<>();
        query.put("query", sql);
        query.put("resultFormat", "object");

        return druidRouterRestClient
                .post()
                .uri("/druid/v2/sql")
                .header("Content-Type", "application/json")
                .body(query)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get segment metadata for a specific datasource using SQL
     */
    public JsonNode getSegmentMetadataForDatasource(String datasourceName) throws RestClientException {
        String sql = "SELECT * FROM sys.segments WHERE datasource = '" + datasourceName + "' ORDER BY \"start\", \"end\"";

        Map<String, Object> query = new HashMap<>();
        query.put("query", sql);
        query.put("resultFormat", "object");

        return druidRouterRestClient
                .post()
                .uri("/druid/v2/sql")
                .header("Content-Type", "application/json")
                .body(query)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get load queue status
     */
    public JsonNode getLoadQueueStatus() throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/loadqueue")
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get load queue status for a specific server
     */
    public JsonNode getLoadQueueStatusForServer(String serverName) throws RestClientException {
        return druidRouterRestClient
                .get()
                .uri("/druid/coordinator/v1/loadqueue/{serverName}", serverName)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }
}
