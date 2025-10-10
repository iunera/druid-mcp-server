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

import jakarta.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@ConditionalOnProperty(prefix = "druid.mcp.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MetricsAspect {

    private static final Logger log = LoggerFactory.getLogger(MetricsAspect.class);

    private final MetricsService metricsService;

    protected final boolean isEnabled = true;

    public MetricsAspect(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Around("@annotation(org.springaicommunity.mcp.annotation.McpTool)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        McpTool mcpTool = method.getAnnotation(McpTool.class);

        String toolName = mcpTool.name() != null && !mcpTool.name().isEmpty() ? mcpTool.name() : method.getName();
        long executionTimestamp = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTimeMillis = System.currentTimeMillis() - executionTimestamp;
            metricsService.sendMetric(toolName, ToolMetrics.ToolStatus.SUCCESS, executionTimeMillis, executionTimestamp);
            return result;
        } catch (Throwable throwable) {
            long executionTimeMillis = System.currentTimeMillis() - executionTimestamp;
            metricsService.sendMetric(toolName, ToolMetrics.ToolStatus.ERROR, executionTimeMillis, executionTimestamp);
            throw throwable;
        }
    }


    @PostConstruct
    void logStatus() {
        if (isEnabled) {
            log.info("MCP Metrics enabled (druid.mcp.metrics.enabled=true)");
        }
    }
}
