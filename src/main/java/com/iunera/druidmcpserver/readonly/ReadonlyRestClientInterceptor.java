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

package com.iunera.druidmcpserver.readonly;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * Intercepts all RestClient HTTP requests and enforces read-only mode rules.
 *
 * Rules when read-only is enabled:
 * - Allow all HTTP GET requests
 * - Allow HTTP POST requests only to the exact path "/druid/v2/sql"
 * - Deny HTTP POST requests to "/druid/v2/sql/task" and any other non-GET requests
 */
public class ReadonlyRestClientInterceptor implements ClientHttpRequestInterceptor {

    private final ReadonlyModeProperties readonlyProps;

    public ReadonlyRestClientInterceptor(ReadonlyModeProperties readonlyProps) {
        this.readonlyProps = readonlyProps;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!readonlyProps.isEnabled()) {
            return execution.execute(request, body);
        }

        HttpMethod method = request.getMethod();
        if (method == null) {
            // Defensive: if method cannot be determined, forbid by default in readonly mode
            throw forbidden(request.getURI(), "Unknown HTTP method in read-only mode");
        }

        if (HttpMethod.GET.equals(method)) {
            return execution.execute(request, body);
        }

        if (HttpMethod.POST.equals(method)) {
            String path = normalizePath(request.getURI());
            if ("/druid/v2/sql".equals(path)) {
                return execution.execute(request, body);
            }
            // Explicitly forbid the task endpoint and all others
            throw forbidden(request.getURI(), "HTTP POST is only permitted to /druid/v2/sql in read-only mode");
        }

        // Any other method (PUT, PATCH, DELETE, etc.) is forbidden in read-only mode
        throw forbidden(request.getURI(), "HTTP " + method.name() + " is not permitted in read-only mode");
    }

    private String normalizePath(URI uri) {
        String p = uri.getPath();
        if (p == null || p.isEmpty()) {
            return "/";
        }
        // Remove trailing slash except root
        if (p.length() > 1 && p.endsWith("/")) {
            return p.substring(0, p.length() - 1);
        }
        return p;
    }

    private static IOException forbidden(URI uri, String reason) {
        return new IOException("Request blocked by read-only mode: " + reason + " (URI=" + uri + ")");
    }
}
