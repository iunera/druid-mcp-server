package com.iunera.druidmcpserver.basicsecurity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iunera.druidmcpserver.monitoring.health.repository.HealthStatusRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class SecurityConfigurationTools {

    private final ObjectMapper objectMapper;
    private final SecurityRepository securityRepository;

    public SecurityConfigurationTools(HealthStatusRepository healthStatusRepository, ObjectMapper objectMapper, SecurityRepository securityRepository) {
        this.objectMapper = objectMapper;
        this.securityRepository = securityRepository;
    }

    /**
     * Get configured authenticator chain and authorizers from coordinator properties
     */
    @McpTool(description = "Get configured authenticatorChain and authorizers form the Basic Auth configuration. This information is important for any other security tool and LLMs need to call this tool first.")
    public String getAuthenticatorChainAndAuthorizers() {
        try {
            JsonNode props = securityRepository.getCoordinatorProperties();

            String authChainStr = props.path("druid.auth.authenticatorChain").asText("");
            String authorizersStr = props.path("druid.auth.authorizers").asText("");

            ArrayNode authenticatorChain = objectMapper.createArrayNode();
            ArrayNode authorizers = objectMapper.createArrayNode();

            try {
                if (authChainStr != null && !authChainStr.isEmpty()) {
                    JsonNode parsed = objectMapper.readTree(authChainStr);
                    if (parsed.isArray()) {
                        for (JsonNode n : parsed) {
                            authenticatorChain.add(n.asText());
                        }
                    }
                }
            } catch (Exception ignore) {
                // ignore parsing errors and return empty or partially filled result
            }

            try {
                if (authorizersStr != null && !authorizersStr.isEmpty()) {
                    JsonNode parsed = objectMapper.readTree(authorizersStr);
                    if (parsed.isArray()) {
                        for (JsonNode n : parsed) {
                            authorizers.add(n.asText());
                        }
                    }
                }
            } catch (Exception ignore) {
                // ignore parsing errors
            }

            ObjectNode result = objectMapper.createObjectNode();
            result.set("authenticatorChain", authenticatorChain);
            result.set("authorizers", authorizers);
            return result.toString();
        } catch (RestClientException e) {
            return objectMapper.createObjectNode()
                    .put("error", "Failed to get coordinator properties: " + e.getMessage())
                    .toString();
        }
    }
}
