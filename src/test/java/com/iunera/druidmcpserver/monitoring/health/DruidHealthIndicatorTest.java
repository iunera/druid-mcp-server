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

package com.iunera.druidmcpserver.monitoring.health;

import com.iunera.druidmcpserver.monitoring.health.repository.ClusterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DruidHealthIndicatorTest {

    private ClusterRepository clusterRepository;
    private DruidHealthIndicator druidHealthIndicator;

    @BeforeEach
    void setUp() {
        clusterRepository = mock(ClusterRepository.class);
        druidHealthIndicator = new DruidHealthIndicator(clusterRepository);
    }

    @Test
    void health_Up_WhenMetadataRetrieved() {
        when(clusterRepository.getClusterMetadata()).thenReturn(null); // Return value doesn't matter for success

        Health health = druidHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("Cluster metadata retrieved successfully", health.getDetails().get("druid"));
    }

    @Test
    void health_Down_WhenExceptionOccurs() {
        when(clusterRepository.getClusterMetadata()).thenThrow(new RestClientException("Connection refused"));

        Health health = druidHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Could not reach Druid: Connection refused", health.getDetails().get("error"));
    }
}
