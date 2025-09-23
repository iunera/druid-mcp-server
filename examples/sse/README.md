# SSE Transport Configuration Examples

This directory contains example configuration files for running the Druid MCP Server using SSE (Server-Sent Events) transport mode. SSE transport provides HTTP-based communication and is suitable for web-based integrations and clients that prefer REST API-style connections.

## Configuration Files

### `sse-mcp-servers-config.json`
Basic SSE configuration for connecting to the Druid MCP Server via HTTP.

**Features:**
- HTTP-based connection using Server-Sent Events
- Simple URL-based configuration
- No command-line execution required
- Suitable for web-based MCP clients

**Configuration:**
```json
{
  "mcpServers": {
    "default-server": {
      "type": "sse",
      "url": "http://localhost:8080/sse",
      "note": "For SSE connections, add this URL directly in your MCP Client"
    }
  }
}
```

## SSE vs STDIO Transport

### SSE Transport (Server-Sent Events)
- **Connection Type**: HTTP-based using Server-Sent Events
- **Communication**: RESTful API with persistent HTTP connections
- **Server Mode**: Runs as a web server on specified port
- **Client Integration**: URL-based connection
- **Use Cases**: Web applications, browser-based clients, REST API integrations

### STDIO Transport
- **Connection Type**: Standard Input/Output streams
- **Communication**: Direct process communication
- **Server Mode**: Command-line execution
- **Client Integration**: Process spawning
- **Use Cases**: Desktop applications, CLI tools, direct process integration

## How to Use

### Prerequisites
1. Build the Druid MCP Server:
   ```bash
   mvn clean package -DskipTests
   ```

2. Ensure the JAR file exists at `target/druid-mcp-server-1.2.1.jar`

### Starting the SSE Server

Run the server in SSE mode (default web server mode):
```bash
java -jar target/druid-mcp-server-1.2.1.jar
```

The server will start on port 8080 by default and provide SSE endpoints at:
- **SSE Endpoint**: `http://localhost:8080/sse`
- **Health Check**: `http://localhost:8080/actuator/health` (if actuator is enabled)

### Custom Port Configuration

To run on a different port:
```bash
java -Dserver.port=9090 -jar target/druid-mcp-server-1.2.1.jar
```

Then update your configuration to use `http://localhost:9090/sse`

### Production Configuration

For production environments, consider these additional settings:
```bash
java -Dserver.port=8080 \
     -Ddruid.broker.url=https://your-druid-broker.com:8082 \
     -Ddruid.coordinator.url=https://your-druid-coordinator.com:8081 \
     -Ddruid.auth.username=your-username \
     -Ddruid.auth.password=your-password \
     -Ddruid.ssl.enabled=true \
     -jar target/druid-mcp-server-1.2.1.jar
```

## Configuration Structure

The SSE configuration follows a different pattern than STDIO:

```json
{
  "mcpServers": {
    "server-name": {
      "type": "sse",
      "url": "http://host:port/sse"
    }
  }
}
```

**Key Parameters:**
- `type`: Must be "sse" for Server-Sent Events transport
- `url`: The complete URL to the SSE endpoint
- `server-name`: Identifier for the server (can be customized)

## Integration with MCP Clients

### Web-based Clients
1. Configure your client to connect to the SSE URL
2. Use the URL: `http://localhost:8080/sse`
3. Ensure the server is running before connecting

### Desktop Applications
Some desktop MCP clients support SSE connections:
1. Add the server configuration to your client
2. Use the provided `sse-mcp-servers-config.json` as a template
3. Update the URL if using a custom port

## API Endpoints

When running in SSE mode, the server provides several endpoints:

### SSE Endpoint
- **URL**: `/sse`
- **Method**: GET
- **Description**: Main SSE endpoint for MCP communication
- **Headers**: `Accept: text/event-stream`

### Health Check (if enabled)
- **URL**: `/actuator/health`
- **Method**: GET
- **Description**: Server health status

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Ensure the server is running: `java -jar target/druid-mcp-server-1.2.1.jar`
   - Check if port 8080 is available or use a different port
   - Verify firewall settings allow connections to the port

2. **SSE Connection Drops**
   - Check network stability
   - Verify client supports persistent SSE connections
   - Monitor server logs for errors

3. **Druid Connection Issues**
   - Verify Druid cluster is accessible from the server
   - Check Druid broker/coordinator URLs in configuration
   - Validate authentication credentials if using secured Druid

4. **CORS Issues (for web clients)**
   - The server may need CORS configuration for browser-based clients
   - Add CORS headers if integrating with web applications

### Debug Mode

Enable debug logging:
```bash
java -Dlogging.level.com.iunera.druidmcpserver=DEBUG \
     -jar target/druid-mcp-server-1.2.1.jar
```

### Testing the Connection

Test the SSE endpoint manually:
```bash
curl -H "Accept: text/event-stream" http://localhost:8080/sse
```

## Security Considerations

### Development
- Default configuration uses HTTP (not HTTPS)
- No authentication required for local development
- Suitable for local testing only

### Production
- **Use HTTPS**: Configure SSL/TLS for production deployments
- **Authentication**: Implement proper authentication mechanisms
- **Network Security**: Use firewalls and network segmentation
- **Druid Security**: Ensure Druid cluster has proper authentication
- **Monitoring**: Implement logging and monitoring for security events

### Recommended Production Setup
```bash
java -Dserver.port=8443 \
     -Dserver.ssl.enabled=true \
     -Dserver.ssl.key-store=path/to/keystore.p12 \
     -Dserver.ssl.key-store-password=your-password \
     -Ddruid.broker.url=https://secure-druid-broker.com:8082 \
     -jar target/druid-mcp-server-1.2.1.jar
```

## Performance Considerations

- **Connection Pooling**: SSE maintains persistent connections
- **Resource Usage**: Monitor memory usage with multiple concurrent connections
- **Scaling**: Consider load balancing for high-traffic scenarios
- **Timeouts**: Configure appropriate connection timeouts

## Related Documentation

- [Main README](../../README.md) - Project overview and setup
- [Development Guide](../../development.md) - Development guidelines and testing
- [STDIO Examples](../stdio/) - Command-line transport configuration examples
- [Druid Cluster Setup](../druidcluster/) - Local Druid cluster for testing

## Comparison with STDIO

| Feature | SSE Transport | STDIO Transport |
|---------|---------------|-----------------|
| Connection | HTTP-based | Process-based |
| Integration | URL configuration | Command execution |
| Scalability | Multiple concurrent connections | Single process per client |
| Debugging | HTTP tools, browser dev tools | Process logs |
| Security | HTTPS, web security | Process isolation |
| Use Case | Web apps, REST APIs | Desktop apps, CLI tools |

---

## About iunera

This SSE transport configuration example is part of the Druid MCP Server developed and maintained by **[iunera](https://www.iunera.com)**, a leading provider of advanced AI and data analytics solutions.

For more information about our enterprise solutions and professional services, visit [www.iunera.com](https://www.iunera.com).

---

*© 2024 [iunera](https://www.iunera.com). Licensed under the Apache License 2.0.*
