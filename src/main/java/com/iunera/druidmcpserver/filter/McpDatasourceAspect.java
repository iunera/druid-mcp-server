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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class McpDatasourceAspect {

    private final McpToolProperties properties;

    public McpDatasourceAspect(McpToolProperties properties) {
        this.properties = properties;
    }

    @Around("@annotation(org.springframework.ai.mcp.annotation.McpTool)")
    public Object filterByDatasource(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        McpTool mcpTool = method.getAnnotation(McpTool.class);
        String toolName = mcpTool.name() != null && !mcpTool.name().isEmpty() ? mcpTool.name() : method.getName();

        // 1. Identify if a datasource argument exists in the parameters
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        String datasourceVal = null;

        if (parameterNames != null && args != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                String paramName = parameterNames[i];
                if ("datasourceName".equalsIgnoreCase(paramName) || "datasource".equalsIgnoreCase(paramName)) {
                    if (args[i] instanceof String) {
                        datasourceVal = (String) args[i];
                        break;
                    }
                }
            }
        }

        // 2. Enforce limits if a datasource is specified and configured
        if (datasourceVal != null && properties.getDatasourceLimits() != null) {
            // Find exact match in limits keys
            McpToolProperties.DatasourceLimitProperties limits = properties.getDatasourceLimits().get(datasourceVal);

            if (limits != null) {
                // Check if this specific tool is enabled for the datasource (exact whitelist match only)
                boolean isEnabled = false;
                if (limits.getEnabled() != null) {
                    if (limits.getEnabled().contains(toolName)) {
                        isEnabled = true;
                    }
                }

                if (!isEnabled) {
                    throw new IllegalArgumentException(String.format(
                            "Tool '%s' is not enabled for datasource '%s' by configuration limits.",
                            toolName, datasourceVal));
                }
            }
        }

        return joinPoint.proceed();
    }
}
