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
import com.iunera.druidmcpserver.datamanagement.query.QueryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class ReadonlyWebRequestAspect {

    private final ReadonlyModeProperties readonlyProps;

    public ReadonlyWebRequestAspect(ReadonlyModeProperties readonlyProps) {
        this.readonlyProps = readonlyProps;
    }

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object enforceReadonlyForHttp(ProceedingJoinPoint pjp) throws Throwable {
        if (!readonlyProps.isEnabled()) {
            return pjp.proceed();
        }

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            // Not an HTTP request context; proceed
            return pjp.proceed();
        }
        HttpServletRequest request = servletAttrs.getRequest();

        String method = request.getMethod();
        String uri = request.getRequestURI();

        if ("GET".equalsIgnoreCase(method)) {
            return pjp.proceed();
        }

        // Allow only exact POST to /druid/v2/sql (no subpaths)
        if ("POST".equalsIgnoreCase(method)) {
            if (uri != null) {
                // normalize potential trailing slash
                String normalized = uri.endsWith("/") && uri.length() > 1 ? uri.substring(0, uri.length() - 1) : uri;
                if (QueryRepository.SQL_ENDPOINT.equals(normalized)) {
                    return pjp.proceed();
                }
                if (normalized != null && normalized.startsWith(QueryRepository.SQL_ENDPOINT + "/")) {
                    throw readonlyDenied("POST to subpath of /druid/v2/sql is not allowed in read-only mode", method, uri);
                }
            }
        }

        throw readonlyDenied("Only HTTP GET is allowed in read-only mode (plus POST to /druid/v2/sql)", method, uri);
    }

    private ResponseStatusException readonlyDenied(String message, String method, String uri) {
        String details = String.format("%s. Request: %s %s. Allowed: GET, POST %s", message, method, uri, QueryRepository.SQL_ENDPOINT);
        return new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, details);
    }
}
