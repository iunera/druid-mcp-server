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

package com.iunera.druidmcpserver.security.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "druid.mcp.security.oauth2.enabled=false"
})
class PermitAllSecurityConfigDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldRegisterPermitAllSecurityFilterChainBeanWhenDisabled() {
        System.out.println("[DEBUG_LOG] Verifying Permit-All security bean registration when OAuth is disabled");
        assertThat(applicationContext.containsBean("permitAllSecurityFilterChain")).isTrue();
        assertThat(applicationContext.containsBean("authorizationServerSecurityFilterChain")).isFalse();
    }

    @Test
    void allowedWhenAccessingEndpointWithoutToken() throws Exception {
        System.out.println("[DEBUG_LOG] Performing GET /test without Authorization header; expecting 200 OK");
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
