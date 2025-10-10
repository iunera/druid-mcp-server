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

package com.iunera.druidmcpserver.usage.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);
    private final RestTemplate restTemplate;
    private final String metricsUrl;
    private final String serverName;
    private final String serverVersion;
    private final String protocol;
    private final Boolean oauth2Enabled;
    private final Boolean readonlyEnabled;

    public MetricsService(RestTemplate restTemplate,
                          @Value("${spring.ai.mcp.metrics.url:https://mcpmetrics.k8s.iunera.com/druid/v1}") String metricsUrl,
                          @Value("${spring.application.name:unknown}") String serverName,
                          @Value("${spring.ai.mcp.server.version:unknown}") String serverVersion,
                          @Value("${spring.ai.mcp.server.protocol:unknown}") String protocol,
                          @Value("${druid.mcp.security.oauth2.enabled:false}") Boolean oauth2Enabled,
                          @Value("${druid.mcp.readonly.enabled:false}") Boolean readonlyEnabled) {
        this.restTemplate = restTemplate;
        this.metricsUrl = metricsUrl;
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.protocol = protocol;
        this.oauth2Enabled = oauth2Enabled;
        this.readonlyEnabled = readonlyEnabled;
    }

    public static Boolean isRunningInsideDocker() {
        try (java.util.stream.Stream<String> stream = java.nio.file.Files.lines(java.nio.file.Paths.get("/proc/1/cgroup"))) {
            return stream.anyMatch(line -> line.contains("docker"));
        } catch (java.io.IOException e) {
            return false;
        }
    }

    public void sendMetric(String toolName, ToolMetrics.ToolStatus status, long executionTimeMillis, long executionTimestamp) {
        try {
            String hostnameHash = getHostnameHash();
            Boolean isDocker = isRunningInsideDocker();
            ToolMetrics metric = new ToolMetrics(serverName, toolName, status, hostnameHash, serverVersion, executionTimeMillis, executionTimestamp, isDocker, protocol, oauth2Enabled, readonlyEnabled, "1.0.0");

            restTemplate.put(metricsUrl + "/" + metric.metricsVersion(), metric);
        } catch (Exception _) {
        }
    }

    private String getHostnameHash() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(hostName.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (UnknownHostException | NoSuchAlgorithmException e) {
            log.warn("Could not determine hostname hash", e);
            return "unknown";
        }
    }
}
