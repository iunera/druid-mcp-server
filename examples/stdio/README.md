# STDIO (Docker) Examples

This folder contains Docker-focused examples for running the Druid MCP Server with the STDIO transport. These examples are designed to be used by MCP clients (e.g., Claude Desktop) that spawn the server process and communicate over STDIO.

We focus on environment variable configuration. No Java/JAR commands are required.

## Quick use with MCP client

Point your MCP client to this config file:
- [mcpservers-stdio.json](mcpservers-stdio.json)

That file makes your client start the server using Docker with all important environment variables.

## What the config does

- Uses Docker image: iunera/druid-mcp-server:1.2.1
- Starts the container in STDIO mode via environment variables
- Passes Druid connection settings via environment variables

Key environment variables you can override in your MCP client:
- DRUID_ROUTER_URL (default: http://host.docker.internal:8888)
- DRUID_AUTH_USERNAME
- DRUID_AUTH_PASSWORD
- DRUID_SSL_ENABLED (default: false)
- DRUID_SSL_SKIP_VERIFICATION (default: true)
- DRUID_MCP_READONLY (default: false)

See [mcpservers-stdio.json](mcpservers-stdio.json) for defaults and how these are passed to Docker.

## Direct Docker run (optional)
If your client does not support spawning via config, you can also run the server manually and point the client to the spawned process (stdin/stdout attached):

```bash
docker run --rm -i \
  -e SPRING_AI_MCP_SERVER_STDIO=true \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  -e LOGGING_PATTERN_CONSOLE= \
  -e DRUID_ROUTER_URL=http://host.docker.internal:8888 \
  iunera/druid-mcp-server:1.2.1
```

Tip: Replace DRUID_ROUTER_URL and add DRUID_AUTH_USERNAME/DRUID_AUTH_PASSWORD and TLS flags when connecting to a secured Druid.
