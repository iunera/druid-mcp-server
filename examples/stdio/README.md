# STDIO Examples (JBang & Docker)

This folder contains examples for running the Druid MCP Server with the STDIO transport. These examples are designed to be used by MCP clients (e.g., Claude Desktop) that spawn the server process and communicate over STDIO.

## Option 1: Running with JBang (Recommended)

JBang is the easiest way to run the server as it automatically manages the Java environment and downloads the required version.

### Quick use with MCP client

Point your MCP client to this config file:
- [mcpservers-jbang.json](mcpservers-jbang.json)

The JBang configuration in that file allows the client to start the server directly using the Maven GAV.

### Direct JBang run (optional)

```bash
export DRUID_ROUTER_URL=http://localhost:8888
export DRUID_AUTH_USERNAME=admin
export DRUID_AUTH_PASSWORD=password
jbang druid-mcp-server@iunera/druid-mcp-server
```

## Option 2: Running with Docker

Docker is a great alternative if you prefer containerized execution.

### Quick use with MCP client

Point your MCP client to this config file:
- [mcpservers-docker.json](mcpservers-docker.json)

### Direct Docker run (optional)
If your client does not support spawning via config, you can also run the server manually and point the client to the spawned process (stdin/stdout attached):

```bash
docker run --rm -i \
  -e DRUID_ROUTER_URL=http://host.docker.internal:8888 \
  -e DRUID_COORDINATOR_URL=http://host.docker.internal:8081 \
  -e DRUID_AUTH_USERNAME=admin \
  -e DRUID_AUTH_PASSWORD=password \
  iunera/druid-mcp-server:1.7.0
```

Tip: Replace DRUID_ROUTER_URL and add DRUID_AUTH_USERNAME/DRUID_AUTH_PASSWORD and TLS flags when connecting to a secured Druid.
