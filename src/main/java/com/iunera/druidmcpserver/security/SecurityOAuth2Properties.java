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

package com.iunera.druidmcpserver.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties controlling OAuth2 security for the application.
 */
@Component
@ConfigurationProperties(prefix = "druid.mcp.security.oauth2")
public class SecurityOAuth2Properties {

    private static final Logger log = LoggerFactory.getLogger(SecurityOAuth2Properties.class);

    /**
     * Enable OAuth2 security. When false, all HTTP requests are permitted without authentication.
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @PostConstruct
    void logStatus() {
        if (enabled) {
            log.info("OAuth2 security is ENABLED (druid.mcp.security.oauth2.enabled=true)");
        } else {
            log.warn("OAuth2 security is DISABLED (druid.mcp.security.oauth2.enabled=false). All HTTP requests are permitted without authentication.");
        }
    }
}
