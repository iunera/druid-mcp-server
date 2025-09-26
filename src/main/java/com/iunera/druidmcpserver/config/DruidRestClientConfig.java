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

package com.iunera.druidmcpserver.config;

import com.iunera.druidmcpserver.readonly.ReadonlyModeProperties;
import com.iunera.druidmcpserver.readonly.ReadonlyRestClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;

@Configuration
public class DruidRestClientConfig {

    private final DruidProperties druidProperties;
    private final ReadonlyModeProperties readonlyModeProperties;

    public DruidRestClientConfig(DruidProperties druidProperties, ReadonlyModeProperties readonlyModeProperties) {
        this.druidProperties = druidProperties;
        this.readonlyModeProperties = readonlyModeProperties;
    }

    @Bean("druidRouterRestClient")
    public RestClient druidRouterRestClient() {
        return createRestClient(druidProperties.getRouter().getUrl());
    }

    @Bean("druidCoordinatorRestClient")
    public RestClient druidCoordinatorRestClient() {
        return createRestClient(druidProperties.getCoordinator().getUrl());
    }

    private RestClient createRestClient(String baseUrl) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl);

        // Configure HTTP client for SSL settings
        if (druidProperties.getSsl().isEnabled() || druidProperties.getSsl().isSkipVerification()) {
            HttpClient httpClient = createHttpClient();
            builder = builder.requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient));
        }

        // Enforce read-only rules via interceptor
        builder = builder.requestInterceptor(new ReadonlyRestClientInterceptor(readonlyModeProperties));

        // Add basic authentication if credentials are provided
        if (druidProperties.getAuth().getUsername() != null && druidProperties.getAuth().getPassword() != null) {
            builder = builder.requestInterceptor(createBasicAuthInterceptor());
        }

        return builder.build();
    }

    private HttpClient createHttpClient() {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30));

        if (druidProperties.getSsl().isSkipVerification()) {
            try {
                // Create a trust manager that accepts all certificates
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                httpClientBuilder.sslContext(sslContext);
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure SSL context for skip verification", e);
            }
        }
        // If sslEnabled is true but skipSslVerification is false, use system default truststore
        // This is the default behavior of HttpClient, so no additional configuration needed

        return httpClientBuilder.build();
    }

    private ClientHttpRequestInterceptor createBasicAuthInterceptor() {
        return (request, body, execution) -> {
            String auth = druidProperties.getAuth().getUsername() + ":" + druidProperties.getAuth().getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            request.getHeaders().add("Authorization", "Basic " + encodedAuth);
            return execution.execute(request, body);
        };
    }

    public String getDruidRouterUrl() {
        return druidProperties.getRouter().getUrl();
    }

    public String getDruidUsername() {
        return druidProperties.getAuth().getUsername();
    }

    public String getDruidPassword() {
        return druidProperties.getAuth().getPassword();
    }

    public boolean isSkipSslVerification() {
        return druidProperties.getSsl().isSkipVerification();
    }

    public boolean isSslEnabled() {
        return druidProperties.getSsl().isEnabled();
    }
}
