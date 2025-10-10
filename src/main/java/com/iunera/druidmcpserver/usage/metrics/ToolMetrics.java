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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ToolMetrics(
        @JsonProperty("servername") String serverName,
        @JsonProperty("tool_name") String toolName,
        @JsonProperty("tool_status") ToolStatus toolStatus,
        @JsonProperty("hostname_hash") String hostnameHash,
        @JsonProperty("server_version") String serverVersion,
        @JsonProperty("execution_time_millis") long executionTimeMillis,
        @JsonProperty("execution_timestamp") long executionTimestamp,
        @JsonProperty("is_running_inside_docker") Boolean isRunningInsideDocker,
        @JsonProperty("protocol") String protocol,
        @JsonProperty("oauth2_enabled") Boolean oauth2Enabled,
        @JsonProperty("readonly_enabled") Boolean readonlyEnabled,
        @JsonProperty("metricsVersion") String metricsVersion
) {
    public enum ToolStatus {
        SUCCESS,
        ERROR
    }
}
