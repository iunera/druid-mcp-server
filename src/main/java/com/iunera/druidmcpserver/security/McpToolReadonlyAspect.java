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

import com.iunera.druidmcpserver.config.ReadonlyModeProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class McpToolReadonlyAspect {

    private final ReadonlyModeProperties readonlyProps;

    public McpToolReadonlyAspect(ReadonlyModeProperties readonlyProps) {
        this.readonlyProps = readonlyProps;
    }

    @Around("@annotation(org.springaicommunity.mcp.annotation.McpTool)")
    public Object enforceReadonlyForMcpTools(ProceedingJoinPoint pjp) throws Throwable {
        if (!readonlyProps.isEnabled()) {
            return pjp.proceed();
        }

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String methodName = sig.getMethod().getName();

        if (isAllowedReadonlyTool(methodName)) {
            return pjp.proceed();
        }

        // Most MCP tools in this project return String; if not, fall back to a generic message
        String errorJson = "{\"error\":\"read_only_mode\",\"message\":\"This tool is disabled in read-only mode: "
                + methodName +
                "\",\"allowedToolsHint\":\"Use only read-only tools: queryDruidSql, get*, list*, show*, view*, health*, status*\"}";
        Class<?> returnType = sig.getReturnType();
        if (String.class.equals(returnType)) {
            return errorJson;
        }
        // If some tool returns non-String, we throw an exception to be safe
        throw new IllegalStateException("MCP tool '" + methodName + "' blocked by read-only mode.");
    }

    private boolean isAllowedReadonlyTool(String name) {
        if (name == null) return false;
        String n = name;
        if (n.equals("queryDruidSql")) return true; // specifically allowed
        if (n.equals("getMultiStageQueryTaskStatus")) return true; // read-only status check
        // Allow common read-only prefixes
        return n.startsWith("get") || n.startsWith("list") || n.startsWith("show") || n.startsWith("view")
                || n.startsWith("health") || n.startsWith("status");
    }
}
