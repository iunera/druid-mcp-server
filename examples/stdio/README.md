# STDIO (Docker) Examples

This folder contains Docker-focused examples for running the Druid MCP Server with the STDIO transport. These examples are designed to be used by MCP clients (e.g., Claude Desktop) that spawn the server process and communicate over STDIO.

We focus on environment variable configuration. No Java/JAR commands are required.

## Quick use with MCP client

Point your MCP client to this config file:
- [mcpservers-stdio.json](mcpservers-stdio.json)

That file makes your client start the server using Docker with all important environment variables.

## What the config does

- Uses Docker image: iunera/druid-mcp-server:1.5.2
- Starts the container in STDIO mode via environment variables
- Passes Druid connection settings via environment variables

See [mcpservers-stdio.json](mcpservers-stdio.json) for defaults and how these are passed to Docker.

## Direct Docker run (optional)
If your client does not support spawning via config, you can also run the server manually and point the client to the spawned process (stdin/stdout attached):

```bash
docker run --rm -i \
  -e DRUID_ROUTER_URL=http://host.docker.internal:8888 \
  -e DRUID_COORDINATOR_URL=http://host.docker.internal:8081 \
  -e DRUID_AUTH_USERNAME=admin \
  -e DRUID_AUTH_PASSWORD=password \
  iunera/druid-mcp-server:1.5.2
```

Tip: Replace DRUID_ROUTER_URL and add DRUID_AUTH_USERNAME/DRUID_AUTH_PASSWORD and TLS flags when connecting to a secured Druid.
