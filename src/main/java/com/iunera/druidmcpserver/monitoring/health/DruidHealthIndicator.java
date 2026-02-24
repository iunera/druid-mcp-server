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
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Apache Druid cluster.
 * It considers the service healthy if it can successfully retrieve cluster metadata.
 */
@Component
@Profile("http")
public class DruidHealthIndicator implements HealthIndicator {

    private final ClusterRepository clusterRepository;

    public DruidHealthIndicator(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    @Override
    public Health health() {
        try {
            clusterRepository.getClusterMetadata();
            return Health.up()
                    .withDetail("druid", "Cluster metadata retrieved successfully")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Could not reach Druid: " + e.getMessage())
                    .build();
        }
    }
}
