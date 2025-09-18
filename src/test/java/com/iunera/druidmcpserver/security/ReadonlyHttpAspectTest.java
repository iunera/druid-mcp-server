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

import com.iunera.druidmcpserver.datamanagement.query.QueryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "druid.mcp.readonly.enabled=true"
})
@Import(ReadonlyHttpAspectTest.TestController.class)
class ReadonlyHttpAspectTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @RestController
    static class TestController {
        @GetMapping("/hello")
        public String hello() { return "ok"; }

        @PostMapping("/echo")
        public String echo(@RequestBody(required = false) String body) { return body == null ? "" : body; }

        @PostMapping(QueryRepository.SQL_ENDPOINT)
        public String sql(@RequestBody(required = false) String body) { return "sql"; }

        @PostMapping(QueryRepository.SQL_TASK_ENDPOINT)
        public String sqlTask(@RequestBody(required = false) String body) { return "sqlTask"; }
    }

    @Test
    void getIsAllowedWhenReadonly() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/hello"), String.class);
        System.out.println("[DEBUG_LOG] GET /hello status=" + response.getStatusCode());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void postSqlExactPathIsAllowedWhenReadonly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"query\":\"SELECT 1\"}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url(QueryRepository.SQL_ENDPOINT), entity, String.class);
        System.out.println("[DEBUG_LOG] POST " + QueryRepository.SQL_ENDPOINT + " status=" + response.getStatusCode());
        assertEquals(HttpStatus.OK, response.getStatusCode(), "POST to exact /druid/v2/sql should be allowed");
    }

    @Test
    void postSqlTaskSubpathIsDeniedWhenReadonly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url(QueryRepository.SQL_TASK_ENDPOINT), entity, String.class);
        System.out.println("[DEBUG_LOG] POST " + QueryRepository.SQL_TASK_ENDPOINT + " status=" + response.getStatusCode());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode(), "POST to /druid/v2/sql/task should be denied");
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("read_only_mode"));
    }

    @Test
    void postArbitraryPathIsDeniedWhenReadonly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"x\":1}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url("/echo"), entity, String.class);
        System.out.println("[DEBUG_LOG] POST /echo status=" + response.getStatusCode());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }
}
