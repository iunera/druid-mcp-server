# STDIO Transport with Docker Configuration Examples

This directory contains example configuration files for running the Druid MCP Server using Docker with STDIO (Standard Input/Output) transport mode. This approach combines the convenience of Docker deployment with the STDIO transport recommended for LLM clients that support the Model Context Protocol (MCP).

## Overview

This example demonstrates how to:
- Run the Druid MCP Server using the pre-built Docker image from Docker Hub
- Configure STDIO transport mode for MCP client integration
- Set up both development and production configurations

## Configuration Files

### `stdio-docker-mcp-servers-config.json`
Basic development configuration using Docker for local testing and development environments.

**Features:**
- Uses Docker instead of local Java installation
- Connects to local Druid cluster via `host.docker.internal`
- No authentication or SSL configuration
- Suitable for development and testing


### `prod-stdio-docker-mcp-servers-config.json`
Production-ready configuration with Docker, security, and authentication settings.

**Features:**
- HTTPS connection to remote Druid cluster
- Authentication credentials configuration via environment variables
- SSL enabled for secure connections
- Production-grade security settings

**Configuration Parameters:**
- `DRUID_ROUTER_URL`: HTTPS URL to your Druid router/broker
- `DRUID_AUTH_USERNAME`: Authentication username
- `DRUID_AUTH_PASSWORD`: Authentication password
- `DRUID_SSL_ENABLED`: Enables SSL/TLS connections


## How to Use

### Prerequisites
1. **Docker installed and running**
2. **Druid cluster accessible** (local or remote)
3. **MCP client** that supports STDIO transport (e.g., Claude Desktop)

### Method 1: Using MCP Client Configuration

#### For Development
1. Copy `stdio-docker-mcp-servers-config.json` to your MCP client configuration directory
2. Update the Druid URL if your cluster is not running on default port:
   ```bash
   "-e", "DRUID_ROUTER_URL=http://your-druid-host:8888"
   ```
3. Configure your MCP client to use this server

#### For Production
1. Copy `prod-stdio-docker-mcp-servers-config.json` to your MCP client configuration
2. Update the configuration parameters:
   - Replace `https://druid.example.com` with your actual Druid URL
   - Replace `mcp-user` and `mcp-user-password` with your credentials
3. Ensure your Druid cluster is accessible and properly configured

### Method 2: Direct Docker Command

#### Development
```bash
docker run --rm -i \
  -e SPRING_AI_MCP_SERVER_STDIO=true \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  -e LOGGING_PATTERN_CONSOLE= \
  -e DRUID_ROUTER_URL=http://host.docker.internal:8888 \
  iunera/druid-mcp-server:1.2.1
```

#### Production
```bash
docker run --rm -i \
  -e SPRING_AI_MCP_SERVER_STDIO=true \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  -e LOGGING_PATTERN_CONSOLE= \
  -e DRUID_ROUTER_URL=https://your-druid-cluster.com \
  -e DRUID_AUTH_USERNAME=your-username \
  -e DRUID_AUTH_PASSWORD=your-password \
  -e DRUID_SSL_ENABLED=true \
  iunera/druid-mcp-server:1.2.1
```

## Configuration Structure

The MCP configuration files follow the standard MCP server configuration format but use Docker commands:

```json
{
  "mcpServers": {
    "druid-mcp-server": {
      "command": "docker",
      "args": [
        "run", "--rm", "-i",
        "-e", "SPRING_AI_MCP_SERVER_STDIO=true",
        "-e", "SPRING_MAIN_WEB_APPLICATION_TYPE=none",
        "-e", "LOGGING_PATTERN_CONSOLE=",
        "-e", "DRUID_ROUTER_URL=http://host.docker.internal:8888",
        "iunera/druid-mcp-server:1.2.1"
      ]
    }
  }
}
```

## Key Docker STDIO Transport Settings

The following environment variables are essential for STDIO transport with Docker:

- `SPRING_AI_MCP_SERVER_STDIO=true`: Enables STDIO transport mode
- `SPRING_MAIN_WEB_APPLICATION_TYPE=none`: Disables web server (required for STDIO)
- `LOGGING_PATTERN_CONSOLE=`: Disables console logging pattern (required for clean STDIO communication)

## Docker-Specific Considerations

### Network Connectivity
- **Local Druid**: Use `host.docker.internal` to connect to services running on the host
- **Remote Druid**: Use the actual hostname or IP address
- **Docker Network**: If Druid is also running in Docker, use service names or container names

### Container Lifecycle
- The `--rm` flag ensures containers are automatically removed after use
- The `-i` flag enables interactive mode required for STDIO communication
- No port mapping is needed since STDIO doesn't use HTTP endpoints

### Environment Variables
All configuration is done through environment variables, making it easy to:
- Override settings without rebuilding images
- Use with container orchestration platforms
- Manage secrets securely in production

## Troubleshooting

### Common Issues

1. **Docker image not found**: Ensure you have internet connectivity to pull from Docker Hub
   ```bash
   docker pull iunera/druid-mcp-server:1.2.1
   ```

2. **Connection refused**: Verify your Druid cluster is running and accessible
   - For local Druid: Check if services are running on expected ports
   - For remote Druid: Verify network connectivity and firewall settings

3. **Authentication failed**: Check your username/password in the production configuration

4. **SSL errors**: Ensure your Druid cluster supports HTTPS and certificates are valid

5. **STDIO communication issues**: Verify that:
   - `-i` flag is used in direct Docker commands
   - Console logging is disabled (`LOGGING_PATTERN_CONSOLE=`)

### Debug Mode

To enable debug logging, add the following environment variable:
```yaml
- LOGGING_LEVEL_COM_IUNERA_DRUIDMCPSERVER=DEBUG
```

Or in Docker command:
```bash
-e LOGGING_LEVEL_COM_IUNERA_DRUIDMCPSERVER=DEBUG
```

### Container Inspection

To inspect the running container:
```bash
# List running containers
docker ps

# View container logs
docker logs druid-mcp-server-stdio

# Execute commands in the container
docker exec -it druid-mcp-server-stdio /bin/bash
```

## Integration with MCP Clients

### Claude Desktop
1. Locate your Claude Desktop configuration file
2. Add the server configuration to the `mcpServers` section
3. Restart Claude Desktop
4. The Docker container will be started automatically when Claude connects

### Other MCP Clients
Refer to your specific MCP client documentation for configuration instructions. The Docker-based approach works with any MCP client that supports STDIO transport.

## Security Considerations

- **Never commit production credentials** to version control
- **Use environment variables** or secure credential management for production deployments
- **Ensure your Druid cluster** has proper authentication and authorization configured
- **Use HTTPS** in production environments
- **Limit Docker permissions** and run containers with appropriate security contexts
- **Keep Docker images updated** by regularly pulling the latest version

## Advantages of Docker + STDIO

### Benefits over JAR-based STDIO
- **No Java installation required** on the client machine
- **Consistent environment** across different operating systems
- **Easy updates** by pulling new Docker images
- **Isolated dependencies** and configuration
- **Better resource management** and cleanup

### Benefits over SSE Transport
- **Direct integration** with MCP clients
- **No HTTP server overhead** for simple use cases
- **Simpler client configuration** for command-line tools
- **Better suited for automation** and scripting scenarios

## Related Documentation

- [Main README](../../README.md) - Project overview and setup
- [Development Guide](../../development.md) - Development guidelines and testing
- [STDIO Examples](../stdio/) - JAR-based STDIO transport configuration examples
- [SSE Examples](../sse/) - HTTP-based transport configuration examples
- [Druid Cluster Setup](../druidcluster/) - Complete Druid cluster with Docker Compose

---

## About iunera

This STDIO Docker transport configuration example is part of the Druid MCP Server developed and maintained by **[iunera](https://www.iunera.com)**, a leading provider of advanced AI and data analytics solutions.

For more information about our enterprise solutions and professional services, visit [www.iunera.com](https://www.iunera.com).

---

*© 2024 [iunera](https://www.iunera.com). Licensed under the Apache License 2.0.*