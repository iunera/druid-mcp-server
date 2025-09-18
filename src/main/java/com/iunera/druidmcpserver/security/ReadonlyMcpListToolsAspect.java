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
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aspect that filters MCP tools discovery results when read-only mode is enabled.
 *
 * This avoids advertising mutating tools to MCP clients by removing them from the
 * tools list returned by the server.
 *
 * The implementation relies on reflection against the MCP schema types to avoid
 * hard dependencies on specific versions. If filtering fails for any reason,
 * it will pass through the original result (conservative fallback).
 */
@Aspect
@Component
public class ReadonlyMcpListToolsAspect {

    private final ReadonlyModeProperties readonlyProps;

    public ReadonlyMcpListToolsAspect(ReadonlyModeProperties readonlyProps) {
        this.readonlyProps = readonlyProps;
    }

    @Around("execution(* *..*listTools(..))")
    public Object filterListedTools(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (!readonlyProps.isEnabled() || result == null) {
            return result;
        }

        try {
            Class<?> resClass = result.getClass();
            // Expecting something like io.modelcontextprotocol.spec.McpSchema$ListToolsResult
            if (!resClass.getName().contains("ListToolsResult")) {
                return result; // Not the expected response type
            }

            // Try to obtain tools list via accessor method: tools() or getTools()
            Method toolsGetter = null;
            for (String name : new String[]{"tools", "getTools"}) {
                try {
                    toolsGetter = resClass.getMethod(name);
                    break;
                } catch (NoSuchMethodException ignored) { }
            }
            if (toolsGetter == null) {
                return result; // can't read tools
            }

            Object toolsObj = toolsGetter.invoke(result);
            if (!(toolsObj instanceof List<?> tools)) {
                return result;
            }

            // Filter tools by name with allowlist heuristic
            List<Object> filtered = new ArrayList<>();
            for (Object tool : tools) {
                String toolName = resolveToolName(tool);
                if (isAllowedReadonlyTool(toolName)) {
                    filtered.add(tool);
                }
            }

            // If unchanged, return original result
            if (filtered.size() == tools.size()) {
                return result;
            }

            // Try to preserve optional cursor if present
            Object nextCursor = null;
            Method nextCursorGetter = null;
            try {
                nextCursorGetter = resClass.getMethod("nextCursor");
                nextCursor = nextCursorGetter.invoke(result);
            } catch (NoSuchMethodException ignored) {
                // Some implementations may not have paging
            }

            // Attempt to construct a new result instance
            Object reconstructed = tryConstructListToolsResult(resClass, filtered, nextCursor);
            if (reconstructed != null) {
                return reconstructed;
            }

            // As a last resort, return original result
            return result;
        } catch (Throwable t) {
            // Conservative fallback on any error
            return result;
        }
    }

    private static String resolveToolName(Object tool) {
        if (tool == null) return null;
        Class<?> tc = tool.getClass();
        for (String m : new String[]{"name", "getName"}) {
            try {
                Method mm = tc.getMethod(m);
                Object v = mm.invoke(tool);
                return Objects.toString(v, null);
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static Object tryConstructListToolsResult(Class<?> resClass, List<Object> filtered, Object nextCursor) {
        // Try common constructor signatures
        try {
            // 1-arg: (List)
            Constructor<?> c1 = resClass.getDeclaredConstructor(List.class);
            c1.setAccessible(true);
            return c1.newInstance(filtered);
        } catch (Exception ignored) { }
        try {
            // 2-arg: (List, String/Object)
            for (Constructor<?> c : resClass.getDeclaredConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length == 2 && List.class.isAssignableFrom(p[0])) {
                    c.setAccessible(true);
                    return c.newInstance(filtered, nextCursor);
                }
            }
        } catch (Exception ignored) { }
        try {
            // 3-arg variant: (List, Object, boolean)
            for (Constructor<?> c : resClass.getDeclaredConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length == 3 && List.class.isAssignableFrom(p[0])) {
                    c.setAccessible(true);
                    Object arg2 = nextCursor;
                    Object arg3 = Boolean.FALSE;
                    return c.newInstance(filtered, arg2, arg3);
                }
            }
        } catch (Exception ignored) { }
        return null;
    }

    private boolean isAllowedReadonlyTool(String name) {
        if (name == null) return false;
        if (name.equals("queryDruidSql")) return true; // specifically allowed
        if (name.equals("getMultiStageQueryTaskStatus")) return true; // read-only status check
        // Allow common read-only prefixes
        return name.startsWith("get") || name.startsWith("list") || name.startsWith("show") || name.startsWith("view")
                || name.startsWith("health") || name.startsWith("status");
    }
}
