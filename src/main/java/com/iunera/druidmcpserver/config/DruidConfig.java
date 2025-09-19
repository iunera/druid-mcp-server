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
import org.springframework.beans.factory.annotation.Value;
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
public class DruidConfig {

    @Value("${druid.router.url}")
    private String druidRouterUrl;

    @Value("${druid.auth.username:#{null}}")
    private String druidUsername;

    @Value("${druid.auth.password:#{null}}")
    private String druidPassword;

    @Value("${druid.ssl.skip-verification:false}")
    private boolean skipSslVerification;

    @Value("${druid.ssl.enabled:false}")
    private boolean sslEnabled;

    private final ReadonlyModeProperties readonlyModeProperties;

    public DruidConfig(ReadonlyModeProperties readonlyModeProperties) {
        this.readonlyModeProperties = readonlyModeProperties;
    }

    @Bean("druidRouterRestClient")
    public RestClient druidRouterRestClient() {
        return createRestClient(druidRouterUrl);
    }

    private RestClient createRestClient(String baseUrl) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl);

        // Configure HTTP client for SSL settings
        if (sslEnabled || skipSslVerification) {
            HttpClient httpClient = createHttpClient();
            builder = builder.requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient));
        }

        // Enforce read-only rules via interceptor
        builder = builder.requestInterceptor(new ReadonlyRestClientInterceptor(readonlyModeProperties));

        // Add basic authentication if credentials are provided
        if (druidUsername != null && druidPassword != null) {
            builder = builder.requestInterceptor(createBasicAuthInterceptor());
        }

        return builder.build();
    }

    private HttpClient createHttpClient() {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30));

        if (skipSslVerification) {
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
            String auth = druidUsername + ":" + druidPassword;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            request.getHeaders().add("Authorization", "Basic " + encodedAuth);
            return execution.execute(request, body);
        };
    }

    public String getDruidRouterUrl() {
        return druidRouterUrl;
    }

    public String getDruidUsername() {
        return druidUsername;
    }

    public String getDruidPassword() {
        return druidPassword;
    }

    public boolean isSkipSslVerification() {
        return skipSslVerification;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }
}
