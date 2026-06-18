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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class McpToolFilterPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(McpToolFilterPostProcessor.class);
    private final McpToolProperties properties;

    public McpToolFilterPostProcessor(McpToolProperties properties) {
        this.properties = properties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if ("toolSpecs".equals(beanName) && bean instanceof List<?> list) {
            if (properties.getEnabled() == null || properties.getEnabled().isEmpty()) {
                return bean;
            }
            List<Object> filtered = new ArrayList<>();
            for (Object obj : list) {
                String name = getToolName(obj);
                if (name == null || properties.getEnabled().contains(name)) {
                    filtered.add(obj);
                }
            }
            log.info("McpToolFilterPostProcessor filtered toolSpecs from {} to {}", list.size(), filtered.size());
            return filtered;
        }
        if ("syncTools".equals(beanName) && bean instanceof List<?> list) {
            if (properties.getEnabled() == null || properties.getEnabled().isEmpty()) {
                return bean;
            }
            List<Object> filtered = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof ToolCallback callback) {
                    if (callback.getToolDefinition() != null) {
                        String name = callback.getToolDefinition().name();
                        if (properties.getEnabled().contains(name)) {
                            filtered.add(callback);
                        }
                    }
                } else {
                    filtered.add(obj);
                }
            }
            log.info("McpToolFilterPostProcessor filtered syncTools from {} to {}", list.size(), filtered.size());
            return filtered;
        }
        if (bean instanceof ToolCallbackProvider provider) {
            return new FilteringToolCallbackProvider(provider, properties);
        }
        return bean;
    }

    private String getToolName(Object spec) {
        try {
            Object tool = spec.getClass().getMethod("tool").invoke(spec);
            return (String) tool.getClass().getMethod("name").invoke(tool);
        } catch (Exception e) {
            return null;
        }
    }

    private static class FilteringToolCallbackProvider implements ToolCallbackProvider {
        private final ToolCallbackProvider delegate;
        private final McpToolProperties properties;

        public FilteringToolCallbackProvider(ToolCallbackProvider delegate, McpToolProperties properties) {
            this.delegate = delegate;
            this.properties = properties;
        }

        @Override
        public ToolCallback[] getToolCallbacks() {
            ToolCallback[] original = delegate.getToolCallbacks();
            if (original == null) {
                return new ToolCallback[0];
            }
            if (properties.getEnabled() == null || properties.getEnabled().isEmpty()) {
                return original; // If whitelist is empty, we default to allowing all tools
            }
            List<ToolCallback> filtered = new ArrayList<>();
            for (ToolCallback callback : original) {
                if (callback.getToolDefinition() != null) {
                    String name = callback.getToolDefinition().name();
                    if (properties.getEnabled().contains(name)) {
                        filtered.add(callback);
                    }
                }
            }
            return filtered.toArray(new ToolCallback[0]);
        }
    }
}
