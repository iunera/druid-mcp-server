# MCP Registry Publishing Guide

This guide provides comprehensive instructions for publishing the Druid MCP Server to the official Model Context Protocol (MCP) Registry.

## Overview

The MCP Registry is a community-driven registry service that enables discovery and management of MCP server implementations. Publishing your server makes it discoverable by MCP clients worldwide and allows users to easily install and configure your server.

**Official Documentation:**
- [Publishing Guide](https://github.com/modelcontextprotocol/registry/blob/main/docs/guides/publishing/publish-server.md)
- [Server JSON Schema](https://github.com/modelcontextprotocol/registry/blob/main/docs/reference/server-json/generic-server-json.md)
- [Registry API Reference](https://github.com/modelcontextprotocol/registry/blob/main/docs/reference/README.md)
- Migration checklist: https://github.com/modelcontextprotocol/registry/blob/main/docs/reference/server-json/CHANGELOG.md#migration-checklist-for-publishers

## Prerequisites

Before publishing to the MCP Registry, ensure you have:

1. **MCP Publisher Tool**: Download and build the `mcp-publisher` tool
2. **Server Configuration**: A properly formatted `server.json` file
3. **Authentication Method**: Choose between DNS verification or GitHub OAuth
4. **Domain Access**: If using DNS authentication, access to DNS records for your domain
5. **Registry Access**: Valid registry URL and permissions

## Installation and Setup

### 1. Download MCP Publisher Tool

```bash
# Clone the MCP registry repository
git clone https://github.com/modelcontextprotocol/registry.git
cd registry/tools/publisher

# Build the publisher tool
./build.sh

# Verify installation
./bin/mcp-publisher --help
```

### 2. Verify Server Configuration

Ensure your `server.json` file follows the MCP registry schema. Key requirements:

- **Naming Convention**: Use format `com.iunera/druid-mcp-server` (domain-based)
- **Version Consistency**: Ensure all versions match across files
- **Complete Metadata**: Include description, repository, and package details
- **Environment Variables**: Define all required and optional variables
- **Transport Configuration**: Proper STDIO transport setup for Docker

Example validation:
```bash
# Validate server.json against schema
curl -s https://static.modelcontextprotocol.io/schemas/2025-09-16/server.schema.json | \
jq '.' > schema.json

# Use a JSON schema validator to check your server.json
```

## Authentication Methods

The MCP Registry supports two authentication methods:

### Method 1: DNS Verification (Recommended for Domain Owners)

This method uses DNS TXT records to prove domain ownership.

#### Step 1: Generate Ed25519 Key Pair

```bash
# Generate private key
openssl genpkey -algorithm Ed25519 -out key.pem

# Set secure permissions
chmod 600 key.pem
```

#### Step 2: Create DNS TXT Record

Generate the DNS TXT record content:

```bash
echo "iunera.com. IN TXT \"v=MCPv1; k=ed25519; p=$(openssl pkey -in key.pem -pubout -outform DER | tail -c 32 | base64)\""
```

**Important**: Add this TXT record to your domain's DNS configuration. The record should be:
- **Type**: TXT
- **Name**: `iunera.com` (or your domain)
- **Value**: `v=MCPv1; k=ed25519; p=<base64-encoded-public-key>`

#### Step 3: Verify DNS Record

Wait for DNS propagation (may take up to 24 hours) and verify:

```bash
# Check DNS record
dig TXT iunera.com

# Or use nslookup
nslookup -type=TXT iunera.com
```

#### Step 4: Login with DNS Authentication

```bash
./mcp-publisher login dns \
  --domain iunera.com \
  --private-key $(openssl pkey -in key.pem -noout -text | grep -A3 "priv:" | tail -n +2 | tr -d ' :\n')
```

### Method 2: GitHub OAuth (Alternative)

For repositories hosted on GitHub:

```bash
# Login with GitHub OAuth
./mcp-publisher login github

# Or force new login session
./mcp-publisher login github --force
```

## Publishing Process

### 1. Prepare Server Configuration

Ensure your `server.json` is up-to-date with the latest version and metadata:

```json
{
  "$schema": "https://static.modelcontextprotocol.io/schemas/2025-09-16/server.schema.json",
  "name": "com.iunera/druid-mcp-server",
  "description": "A comprehensive Model Context Protocol (MCP) server for Apache Druid...",
  "status": "active",
  "version": "1.2.1",
  "packages": [
    {
      "registryType": "oci",
      "registryBaseUrl": "https://docker.io",
      "identifier": "iunera/druid-mcp-server",
      "version": "1.2.1",
      "runtimeHint": "docker",
      "transport": {
        "type": "stdio",
        "command": "docker",
        "args": ["run", "--rm", "-i", "iunera/druid-mcp-server:1.6.0"]
      }
    }
  ]
}
```

### 2. Publish to Registry

```bash
# Publish your server
./mcp-publisher publish  \
  --registry-url "https://registry.modelcontextprotocol.io" \
  --mcp-file "./server.json"

# Alternative with explicit login
./mcp-publisher \
  --registry-url "https://registry.modelcontextprotocol.io" \
  --mcp-file "./server.json" \
  --login
```

### 3. Verify Publication

Check if your server was published successfully:

```bash
# Search for your server
curl -s "https://registry.modelcontextprotocol.io/v0/servers?search=com.iunera/druid-mcp-server" | jq . 

# Get server details by ID (if you have the ID)
curl "https://registry.modelcontextprotocol.io/v0/servers/{server-id}"
```

## Version Management

### Updating Published Servers

When releasing a new version:

1. **Update Version Numbers**: Ensure consistency across:
   - `pom.xml` (project version)
   - `server.json` (both server version and package version)
   - Docker image tags
   - All configuration files

2. **Republish**: Use the same publishing command with updated `server.json`

3. **Verify Update**: Check the registry to confirm the new version is available

### Version Consistency Checklist

- [ ] `pom.xml` version: `1.2.1`
- [ ] `server.json` version: `1.2.1`
- [ ] `server.json` package version: `1.2.1`
- [ ] Docker image tag: `iunera/druid-mcp-server:1.6.0`
- [ ] All example configurations updated

## Troubleshooting

### Common Issues

#### DNS Record Not Found
```bash
# Error: DNS TXT record not found
# Solution: Verify DNS record exists and has propagated
dig TXT iunera.com +short
```

#### Authentication Failed
```bash
# Error: Authentication failed
# Solution: Check private key format and DNS record
openssl pkey -in key.pem -noout -text | head -5
```

#### Invalid Server Configuration
```bash
# Error: Invalid server.json
# Solution: Validate against schema
jsonschema -i server.json schema.json
```

#### Version Mismatch
```bash
# Error: Version conflicts
# Solution: Check all version references
grep -r "1.2." . --include="*.json" --include="*.xml"
```

### Debug Commands

```bash
# Check current authentication status
./mcp-publisher status

# Validate server.json locally
./mcp-publisher validate --mcp-file "./server.json"

# Test registry connection
curl -I "https://registry.modelcontextprotocol.io/v0/servers"
```

## Best Practices

1. **Documentation**: Maintain comprehensive README and documentation
2. **Versioning**: Use semantic versioning (major.minor.patch)
3. **Testing**: Thoroughly test your server before publishing
4. **Security**: Keep private keys secure and never commit them
5. **Updates**: Regularly update your server for security and features
6. **Monitoring**: Monitor your server's usage and performance

## Security Considerations

- **Private Keys**: Store Ed25519 private keys securely
- **Environment Variables**: Use secrets for sensitive configuration
- **Access Control**: Implement proper authentication in your server
- **Updates**: Keep dependencies and base images updated

## Support and Resources

- **MCP Registry Issues**: [GitHub Issues](https://github.com/modelcontextprotocol/registry/issues)
- **Documentation**: [MCP Registry Docs](https://github.com/modelcontextprotocol/registry/tree/main/docs)
- **Community**: [MCP Discord](https://discord.gg/modelcontextprotocol)
- **Server Examples**: [MCP Servers Repository](https://github.com/modelcontextprotocol/servers)

## Next Steps

After successful publication:

1. **Update Documentation**: Add installation instructions to your README
2. **Create Examples**: Provide usage examples and configuration templates  
3. **Monitor Usage**: Track server adoption and user feedback
4. **Iterate**: Continuously improve based on user needs

Your Druid MCP Server is now available in the MCP Registry and discoverable by MCP clients worldwide!