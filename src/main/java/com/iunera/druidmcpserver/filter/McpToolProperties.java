/*
 * Copyright (C) 2026 Christian Schmitt, Tim Frey
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

package com.iunera.druidmcpserver.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "druid.mcp.tools")
public class McpToolProperties {

    private List<String> enabled = new ArrayList<>();
    private Map<String, DatasourceLimitProperties> datasourceLimits = new HashMap<>();

    public List<String> getEnabled() {
        return enabled;
    }

    public void setEnabled(List<String> enabled) {
        this.enabled = enabled;
    }

    public Map<String, DatasourceLimitProperties> getDatasourceLimits() {
        return datasourceLimits;
    }

    public void setDatasourceLimits(Map<String, DatasourceLimitProperties> datasourceLimits) {
        this.datasourceLimits = datasourceLimits;
    }

    public static class DatasourceLimitProperties {
        private List<String> enabled = new ArrayList<>();

        public List<String> getEnabled() {
            return enabled;
        }

        public void setEnabled(List<String> enabled) {
            this.enabled = enabled;
        }
    }
}
