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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for connecting to Apache Druid components.
 *
 * Uses Spring Boot relaxed binding so properties can be configured via
 * application.yaml, JVM system properties, or environment variables
 * without any manual environment access in code.
 */
@Component
@ConfigurationProperties(prefix = "druid")
public class DruidProperties {

    private final Router router = new Router();
    private final Coordinator coordinator = new Coordinator();
    private final Auth auth = new Auth();
    private final Ssl ssl = new Ssl();
    private final Extension extension = new Extension();
    private final Mcp mcp = new Mcp();

    public Router getRouter() {
        return router;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public Auth getAuth() {
        return auth;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public Extension getExtension() {
        return extension;
    }

    public Mcp getMcp() {
        return mcp;
    }

    public static class Router {
        /**
         * Base URL of the Druid Router HTTP endpoint.
         */
        private String url = "http://localhost:8888";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Coordinator {
        /**
         * Base URL of the Druid Coordinator HTTP endpoint.
         */
        private String url = "http://localhost:8081";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Auth {
        /**
         * Optional basic auth username.
         */
        private String username;
        /**
         * Optional basic auth password.
         */
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Ssl {
        /**
         * Enable SSL for HTTP client.
         */
        private boolean enabled = false;
        /**
         * If true, the HTTP client will skip certificate verification.
         */
        private boolean skipVerification = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isSkipVerification() {
            return skipVerification;
        }

        public void setSkipVerification(boolean skipVerification) {
            this.skipVerification = skipVerification;
        }
    }

    /**
     * Druid extensions configuration.
     */
    public static class Extension {
        private final DruidBasicSecurity druidBasicSecurity = new DruidBasicSecurity();

        public DruidBasicSecurity getDruidBasicSecurity() {
            return druidBasicSecurity;
        }
    }

    /**
     * Configuration for Druid Basic Security extension.
     * Maps property: druid.extension.druid-basic-security.enabled
     */
    public static class DruidBasicSecurity {
        /**
         * Enable/disable all Basic Security tools and resources in this server.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * MCP-related configuration properties.
     * Maps properties under `druid.mcp.*`.
     */
    public static class Mcp {
        private final Metrics metrics = new Metrics();

        public Metrics getMetrics() {
            return metrics;
        }

        /**
         * Metrics configuration under `druid.mcp.metrics`.
         */
        public static class Metrics {
            /**
             * Enable/disable Metrics AOP aspect publishing tool execution metrics.
             * Maps property: druid.mcp.metrics.enabled
             */
            private boolean enabled = true;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
    }
}
