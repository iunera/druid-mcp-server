# Inspect the Druid MCP Server with @modelcontextprotocol/inspector

This guide shows how to use the official MCP Inspector to explore and debug the Druid MCP Server over all supported transports: Streamable HTTP (recommended), SSE, and STDIO.

The Inspector provides a web UI and a CLI to list tools/resources/prompts, call tools, inspect schemas, and view raw protocol messages.


## Inspect Streamable HTTP MCP Server

### Start the Druid MCP Server

```bash
java -jar target/druid-mcp-server-1.2.2.jar
```

Or Using Docker (HTTP/SSE):

```bash
docker run --rm -p 8080:8080 \
  -e DRUID_ROUTER_URL=http://host.docker.internal:8888 \
  iunera/druid-mcp-server:1.2.2
```

```bash
# Obtain an access token using the built-in Authorization Server
ACCESS_TOKEN=$(curl -s -XPOST "http://localhost:8080/oauth2/token" \
  --data grant_type=client_credentials \
  --user "oidc-client:secret" | jq -r '.access_token')
export MCP_OAUTH_TOKEN="$ACCESS_TOKEN"
echo $MCP_OAUTH_TOKEN 
```

### Inspect via Streamable HTTP (recommended)
- Server endpoint: http://localhost:8080/mcp
- Adds Authorization header if MCP_OAUTH_TOKEN is set

```bash
npx @modelcontextprotocol/inspector --cli http://localhost:8080/mcp --transport http --method tools/list --header "Authorization: Bearer ${MCP_OAUTH_TOKEN}"
npx @modelcontextprotocol/inspector --cli http://localhost:8080/mcp --method tools/call  --header "Authorization: Bearer ${MCP_OAUTH_TOKEN}" --tool-name listDatasources

npx @modelcontextprotocol/inspector --cli http://localhost:8080/mcp \
  --transport http \
  --header "Authorization: Bearer $MCP_OAUTH_TOKEN" \
  --method tools/call \
  --tool-name queryDruidSql \
  --tool-arg sqlQuery="SELECT 1"
```

## Inspect via STDIO

The Inspector can also launch the server via STDIO using the config file at project root: mcpservers-stdio.json.

CLI examples:
```bash
# List tools
npx @modelcontextprotocol/inspector --cli \
  --config examples/stdio/mcpservers-stdio.json \
  --server druid-mcp-server \
  --method tools/list
```

## References
- MCP Inspector README: https://github.com/modelcontextprotocol/inspector
- MCP Spec (2025-06-18): https://modelcontextprotocol.io/specification/2025-06-18
