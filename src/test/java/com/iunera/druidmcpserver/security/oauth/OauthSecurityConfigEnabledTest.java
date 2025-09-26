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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        // Explicitly enable OAuth2 security (this is the default per config)
        "druid.mcp.security.oauth2.enabled=true"
})
class OauthSecurityConfigEnabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldRegisterAuthorizationServerSecurityFilterChainBeanWhenEnabled() {
        System.out.println("[DEBUG_LOG] Verifying OAuth-enabled security bean registration");
        assertThat(applicationContext.containsBean("authorizationServerSecurityFilterChain")).isTrue();
        assertThat(applicationContext.containsBean("permitAllSecurityFilterChain")).isFalse();
    }

    @Test
    void unauthorizedWhenAccessingEndpointWithoutToken() throws Exception {
        System.out.println("[DEBUG_LOG] Performing GET /test without Authorization header; expecting 401");
        mockMvc.perform(get("/test"))
                .andExpect(status().isUnauthorized());
    }
}
