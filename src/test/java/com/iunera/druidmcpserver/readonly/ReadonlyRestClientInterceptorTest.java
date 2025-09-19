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

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReadonlyRestClientInterceptorTest {

    @Test
    void readonly_allows_get_requests() throws Exception {
        ReadonlyModeProperties props = new ReadonlyModeProperties();
        props.setEnabled(true);
        ReadonlyRestClientInterceptor interceptor = new ReadonlyRestClientInterceptor(props);

        StubHttpRequest request = new StubHttpRequest(HttpMethod.GET, URI.create("http://localhost:8888/druid/status"));
        CountingExecution exec = new CountingExecution();

        ClientHttpResponse response = interceptor.intercept(request, new byte[0], exec);
        System.out.println("[DEBUG_LOG] GET allowed in readonly, execution count=" + exec.count);
        assertEquals(1, exec.count);
        assertEquals(200, response.getStatusCode().value());
        response.close();
    }

    @Test
    void readonly_allows_post_to_sql_endpoint_only() throws Exception {
        ReadonlyModeProperties props = new ReadonlyModeProperties();
        props.setEnabled(true);
        ReadonlyRestClientInterceptor interceptor = new ReadonlyRestClientInterceptor(props);

        StubHttpRequest request = new StubHttpRequest(HttpMethod.POST, URI.create("http://localhost:8888/druid/v2/sql"));
        CountingExecution exec = new CountingExecution();

        ClientHttpResponse response = interceptor.intercept(request, new byte[0], exec);
        System.out.println("[DEBUG_LOG] POST /druid/v2/sql allowed in readonly, execution count=" + exec.count);
        assertEquals(1, exec.count);
        assertEquals(200, response.getStatusCode().value());
        response.close();
    }

    @Test
    void readonly_blocks_post_to_sql_task_endpoint() {
        ReadonlyModeProperties props = new ReadonlyModeProperties();
        props.setEnabled(true);
        ReadonlyRestClientInterceptor interceptor = new ReadonlyRestClientInterceptor(props);

        StubHttpRequest request = new StubHttpRequest(HttpMethod.POST, URI.create("http://localhost:8888/druid/v2/sql/task"));
        CountingExecution exec = new CountingExecution();

        IOException ex = assertThrows(IOException.class, () -> interceptor.intercept(request, new byte[0], exec));
        System.out.println("[DEBUG_LOG] POST /druid/v2/sql/task blocked in readonly: " + ex.getMessage());
        assertEquals(0, exec.count);
    }

    @Test
    void readonly_blocks_other_non_get_methods() {
        ReadonlyModeProperties props = new ReadonlyModeProperties();
        props.setEnabled(true);
        ReadonlyRestClientInterceptor interceptor = new ReadonlyRestClientInterceptor(props);

        // DELETE example
        StubHttpRequest del = new StubHttpRequest(HttpMethod.DELETE, URI.create("http://localhost:8888/druid/coordinator/v1/datasources/foo"));
        CountingExecution exec = new CountingExecution();
        IOException ex1 = assertThrows(IOException.class, () -> interceptor.intercept(del, new byte[0], exec));
        System.out.println("[DEBUG_LOG] DELETE blocked in readonly: " + ex1.getMessage());
        assertEquals(0, exec.count);

        // POST to any other endpoint
        StubHttpRequest post = new StubHttpRequest(HttpMethod.POST, URI.create("http://localhost:8888/druid/v2/other"));
        IOException ex2 = assertThrows(IOException.class, () -> interceptor.intercept(post, new byte[0], exec));
        System.out.println("[DEBUG_LOG] POST other blocked in readonly: " + ex2.getMessage());
        assertEquals(0, exec.count);
    }

    @Test
    void disabled_mode_allows_non_get_requests() throws Exception {
        ReadonlyModeProperties props = new ReadonlyModeProperties();
        props.setEnabled(false);
        ReadonlyRestClientInterceptor interceptor = new ReadonlyRestClientInterceptor(props);

        StubHttpRequest request = new StubHttpRequest(HttpMethod.POST, URI.create("http://localhost:8888/druid/v2/other"));
        CountingExecution exec = new CountingExecution();

        ClientHttpResponse response = interceptor.intercept(request, new byte[0], exec);
        System.out.println("[DEBUG_LOG] Readonly disabled, POST allowed, execution count=" + exec.count);
        assertEquals(1, exec.count);
        assertEquals(200, response.getStatusCode().value());
        response.close();
    }

    // ---- test helpers ----

    static class StubHttpRequest implements HttpRequest {
        private final HttpMethod method;
        private final URI uri;
        private final HttpHeaders headers = new HttpHeaders();

        StubHttpRequest(HttpMethod method, URI uri) {
            this.method = method;
            this.uri = uri;
        }

        @Override
        public HttpMethod getMethod() {
            return method;
        }

        public String getMethodValue() {
            return method.name();
        }

        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return Collections.emptyMap();
        }

        public InputStream getBody() {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    static class CountingExecution implements ClientHttpRequestExecution {
        int count = 0;

        @Override
        public ClientHttpResponse execute(HttpRequest request, byte[] body) {
            count++;
            return new ClientHttpResponse() {
                @Override
                public HttpStatusCode getStatusCode() {
                    return HttpStatusCode.valueOf(200);
                }


                @Override
                public String getStatusText() {
                    return "OK";
                }

                @Override
                public HttpHeaders getHeaders() {
                    return new HttpHeaders();
                }

                @Override
                public InputStream getBody() {
                    return new ByteArrayInputStream(new byte[0]);
                }

                @Override
                public void close() {
                }
            };
        }
    }
}
