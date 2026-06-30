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

package com.iunera.druidmcpserver.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnExpression("!'${druid.coordinator.url:}'.isEmpty()")
public class DruidCoordinatorRestClientConfig {

    private final DruidRestClientConfig restClientConfig;
    private final DruidProperties druidProperties;

    public DruidCoordinatorRestClientConfig(DruidRestClientConfig restClientConfig, DruidProperties druidProperties) {
        this.restClientConfig = restClientConfig;
        this.druidProperties = druidProperties;
    }

    @Bean("druidCoordinatorRestClient")
    public RestClient druidCoordinatorRestClient() {
        return restClientConfig.createRestClient(druidProperties.getCoordinator().getUrl());
    }
}
