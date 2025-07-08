# STDIO Transport Configuration Examples

This directory contains example configuration files for running the Druid MCP Server using STDIO (Standard Input/Output) transport mode. STDIO transport is the recommended method for integrating with LLM clients that support the Model Context Protocol (MCP).

## Configuration Files

### `stdio-mcp-servers-config.json`
Basic development configuration for local testing and development environments.

**Features:**
- Minimal configuration for quick setup
- Uses default local Druid connection settings
- No authentication or SSL configuration
- Suitable for development and testing

**Usage:**
```bash
# Copy this configuration to your MCP client's configuration directory
# The exact location depends on your MCP client (Claude Desktop, etc.)
```

### `prod-stdio-mcp-servers-config-prod.json`
Production-ready configuration with security and authentication settings.

**Features:**
- HTTPS connection to remote Druid cluster
- Authentication credentials configuration
- SSL enabled for secure connections
- Production-grade security settings

**Configuration Parameters:**
- `druid.router.url`: HTTPS URL to your Druid router/broker
- `druid.auth.username`: Authentication username
- `druid.auth.password`: Authentication password
- `druid.ssl.enabled`: Enables SSL/TLS connections

## How to Use

### Prerequisites
1. Build the Druid MCP Server:
   ```bash
   mvn clean package -DskipTests
   ```

2. Ensure the JAR file exists at `target/druid-mcp-server-1.0.0.jar`

### For Development
1. Copy `stdio-mcp-servers-config.json` to your MCP client configuration
2. Update the JAR path if necessary
3. Configure your MCP client to use this server

### For Production
1. Copy `prod-stdio-mcp-servers-config-prod.json` to your MCP client configuration
2. Update the configuration parameters:
   - Replace `https://druid.example.com` with your actual Druid URL
   - Replace `mcp-user` and `mcp-user-password` with your credentials
3. Ensure your Druid cluster is accessible and properly configured

## Configuration Structure

Both configuration files follow the MCP server configuration format:

```json
{
  "mcpServers": {
    "druid-mcp-server": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "target/druid-mcp-server-1.0.0.jar"
      ]
    }
  }
}
```

The `args` array contains Java system properties and arguments needed to run the server in STDIO mode.

## Key STDIO Transport Settings

The following Java system properties are essential for STDIO transport:

- `-Dspring.ai.mcp.server.stdio=true`: Enables STDIO transport mode
- `-Dspring.main.web-application-type=none`: Disables web server (required for STDIO)
- `-Dlogging.pattern.console=`: Disables console logging pattern (required for clean STDIO communication)

## Troubleshooting

### Common Issues

1. **JAR file not found**: Ensure you've built the project and the JAR exists at the specified path
2. **Connection refused**: Verify your Druid cluster is running and accessible
3. **Authentication failed**: Check your username/password in the production configuration
4. **SSL errors**: Ensure your Druid cluster supports HTTPS and certificates are valid

### Debug Mode

To enable debug logging, add the following argument to the configuration:
```json
"-Dlogging.level.com.iunera.druidmcpserver=DEBUG"
```

## Integration with MCP Clients

### Claude Desktop
1. Locate your Claude Desktop configuration file
2. Add the server configuration to the `mcpServers` section
3. Restart Claude Desktop

### Other MCP Clients
Refer to your specific MCP client documentation for configuration instructions.

## Security Considerations

- **Never commit production credentials** to version control
- Use environment variables or secure credential management for production deployments
- Ensure your Druid cluster has proper authentication and authorization configured
- Use HTTPS in production environments

## Related Documentation

- [Main README](../../README.md) - Project overview and setup
- [Development Guide](../../development.md) - Development guidelines and testing
- [SSE Examples](../sse/) - HTTP-based transport configuration examples

---

## About iunera

This STDIO transport configuration example is part of the Druid MCP Server developed and maintained by **[iunera](https://www.iunera.com)**, a leading provider of advanced AI and data analytics solutions.

For more information about our enterprise solutions and professional services, visit [www.iunera.com](https://www.iunera.com).

---

*© 2024 [iunera](https://www.iunera.com). Licensed under the Apache License 2.0.*
