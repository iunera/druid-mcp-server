# Streamable HTTP (Docker) Examples

This folder contains Docker-focused examples for running the Druid MCP Server with the Streamable HTTP transport (MCP 2025-06-18). Clients connect over HTTP to a single /mcp endpoint.

We focus on environment variable configuration. No Java/JAR commands are required.

## Quick use with MCP client

Point your MCP client to this config file:
- [mcpservers-streamable-http.json](mcpservers-streamable-http.json)

This tells your client to connect to the Streamable HTTP endpoint. You must run the server container separately (see below).

### Optional: Enable OAuth2 protection

1) Start the server with OAuth2 enabled:

```bash
docker run --rm -p 8080:8080 \
  -e DRUID_ROUTER_URL=http://host.docker.internal:8888 \
  -e DRUID_MCP_SECURITY_OAUTH2_ENABLED=true \
  iunera/druid-mcp-server:1.2.2
```

2) Obtain a token using client-credentials grant (default client: `oidc-client` / `secret`):

```bash
ACCESS_TOKEN=$(curl -s -XPOST "http://localhost:8080/oauth2/token" \
  --data grant_type=client_credentials \
  --user "oidc-client:secret" | jq -r ".access_token")
```

3) Export it so the MCP config can reference it:

```bash
export MCP_OAUTH_TOKEN="$ACCESS_TOKEN"
```

The provided `mcpservers-streamable-http.json` will forward the token using the `Authorization: Bearer` header.

## Run the server with Docker

Expose the HTTP port and configure Druid access via environment variables:

```bash
docker run --rm -p 8080:8080 \
  -e DRUID_ROUTER_URL=http://host.docker.internal:8888 \
  iunera/druid-mcp-server:1.2.2
```

Common environment variables:
- DRUID_ROUTER_URL (default used above)
- DRUID_AUTH_USERNAME
- DRUID_AUTH_PASSWORD
- DRUID_SSL_ENABLED (true/false)
- DRUID_SSL_SKIP_VERIFICATION (true/false)
- DRUID_MCP_READONLY (true/false)

Streamable HTTP endpoint will be available at:
- http://localhost:8080/mcp

Adjust -p 8080:8080 if you need a different port.

