# Druid MCP Server

[![Docker Image](https://img.shields.io/badge/docker-available-blue.svg)](https://hub.docker.com/r/iunera/druid-mcp-server)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-24-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green.svg)](https://spring.io/projects/spring-boot)

A comprehensive **Model Context Protocol (MCP) server** for Apache Druid that provides extensive tools, resources, and AI-assisted prompts for managing and analyzing Druid clusters. Built with enterprise-grade reliability and performance in mind.

*Developed by [**iunera**](https://www.iunera.com) - Your trusted partner for Advanced AI and Data Analytics Solutions*

## 🚀 Quick Start

### Pull and Run
```bash
# Pull the latest image
docker pull iunera/druid-mcp-server:latest

# Run with default configuration
docker run -p 8080:8080 \
  -e DRUID_BROKER_URL=http://your-druid-broker:8082 \
  -e DRUID_COORDINATOR_URL=http://your-druid-coordinator:8081 \
  iunera/druid-mcp-server:latest
```

### Docker Compose
```yaml
version: '3.8'
services:
  druid-mcp-server:
    image: iunera/druid-mcp-server:latest
    ports:
      - "8080:8080"
    environment:
      - DRUID_BROKER_URL=http://druid-broker:8082
      - DRUID_COORDINATOR_URL=http://druid-coordinator:8081
      - SPRING_AI_MCP_SERVER_NAME=druid-mcp-server
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/mcp/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
```

## 🎯 What is MCP?

The **Model Context Protocol (MCP)** is a standardized protocol that enables AI language models to securely connect to external data sources and tools. This Druid MCP Server acts as a bridge between AI assistants and your Apache Druid clusters, providing:

- **Secure API access** to Druid functionality
- **Structured data exchange** with AI models
- **Context-aware responses** for data analytics queries
- **Enterprise-ready integration** with existing workflows

*Learn more about enterprise AI solutions at [iunera.com](https://www.iunera.com)*

## 🛠️ Comprehensive Tools Summary

### 📊 Data Management Tools (12 tools)
Perfect for data exploration, schema analysis, and datasource lifecycle management:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `listDatasources` | Discover all available datasources | None |
| `showDatasourceDetails` | Get schema and metadata | `datasourceName` |
| `killDatasource` | Permanently remove datasource | `datasourceName`, `interval` |
| `listLookups` | Manage lookup tables | None |
| `getLookupConfig` | Retrieve lookup configuration | `tier`, `lookupName` |
| `updateLookupConfig` | Modify lookup settings | `tier`, `lookupName`, `config` |
| `listAllSegments` | View all data segments | None |
| `getSegmentMetadata` | Analyze segment details | `datasourceName`, `segmentId` |
| `getSegmentsForDatasource` | List datasource segments | `datasourceName` |
| `queryDruidSql` | Execute SQL queries | `sqlQuery` |
| `viewRetentionRules` | Check data retention policies | `datasourceName` (optional) |
| `updateRetentionRules` | Modify retention settings | `datasourceName`, `rules` |

### 🔄 Compaction Management Tools (5 tools)
Optimize storage and query performance through intelligent segment compaction:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `viewAllCompactionConfigs` | List all compaction configurations | None |
| `viewCompactionConfigForDatasource` | Get specific compaction config | `datasourceName` |
| `editCompactionConfigForDatasource` | Update compaction settings | `datasourceName`, `config` |
| `deleteCompactionConfigForDatasource` | Remove compaction config | `datasourceName` |
| `viewCompactionStatus` | Monitor compaction progress | `datasourceName` (optional) |

### 📥 Ingestion Management Tools (10 tools)
Streamline data ingestion with batch and streaming capabilities:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `createBatchIngestionTemplate` | Generate ingestion templates | `datasourceName`, `inputSource`, `timestampColumn` |
| `createIngestionSpec` | Submit ingestion specifications | `specJson` |
| `listSupervisors` | Monitor streaming ingestion | None |
| `getSupervisorStatus` | Check supervisor health | `supervisorId` |
| `suspendSupervisor` | Pause streaming ingestion | `supervisorId` |
| `startSupervisor` | Resume streaming ingestion | `supervisorId` |
| `terminateSupervisor` | Stop streaming ingestion | `supervisorId` |
| `listTasks` | View all ingestion tasks | None |
| `getTaskStatus` | Monitor task progress | `taskId` |
| `shutdownTask` | Stop running tasks | `taskId` |

### 🏥 Monitoring & Health Tools (9 tools)
Ensure optimal cluster performance with comprehensive monitoring:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `checkClusterHealth` | Overall cluster status | None |
| `getServiceStatus` | Individual service health | `serviceType` |
| `getClusterConfiguration` | System configuration | None |
| `runDruidDoctor` | Comprehensive diagnostics | None |
| `analyzePerformanceIssues` | Performance analysis | None |
| `generateHealthReport` | Detailed health assessment | None |
| `testQueryFunctionality` | Validate query services | None |
| `testIngestionFunctionality` | Validate ingestion pipeline | None |
| `validateClusterConnectivity` | Network connectivity check | None |

### 🔐 Basic Security Tools (17 tools)
Manage users, roles, and permissions with the Druid Basic Security extension:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `listAuthenticationUsers` | List all users in the Druid authentication system for a specific authenticator | `authenticatorName` |
| `getAuthenticationUser` | Get details of a specific user from the Druid authentication system | `authenticatorName`, `userName` |
| `createAuthenticationUser` | Create a new user in the Druid authentication system | `authenticatorName`, `userName` |
| `deleteAuthenticationUser` | Delete a user from the Druid authentication system. Use with caution as this action cannot be undone. | `authenticatorName`, `userName` |
| `setUserPassword` | Set or update the password for a user in the Druid authentication system | `authenticatorName`, `userName`, `password` |
| `listAuthorizationUsers` | List all users in the Druid authorization system for a specific authorizer | `authorizerName` |
| `getAuthorizationUser` | Get details of a specific user from the Druid authorization system including their roles | `authorizerName`, `userName` |
| `listRoles` | List all roles in the Druid authorization system for a specific authorizer | `authorizerName` |
| `getRole` | Get details of a specific role from the Druid authorization system including its permissions | `authorizerName`, `roleName` |
| `createAuthorizationUser` | Create a new user in the Druid authorization system | `authorizerName`, `userName` |
| `deleteAuthorizationUser` | Delete a user from the Druid authorization system. Use with caution as this action cannot be undone. | `authorizerName`, `userName` |
| `createRole` | Create a new role in the Druid authorization system | `authorizerName`, `roleName` |
| `deleteRole` | Delete a role from the Druid authorization system. Use with caution as this action cannot be undone. | `authorizerName`, `roleName` |
| `setRolePermissions` | Set permissions for a role in the Druid authorization system. Provide permissions as JSON array. | `authorizerName`, `roleName`, `permissions` |
| `assignRoleToUser` | Assign a role to a user in the Druid authorization system | `authorizerName`, `userName`, `roleName` |
| `unassignRoleFromUser` | Unassign a role from a user in the Druid authorization system | `authorizerName`, `userName`, `roleName` |
| `getAuthenticatorChainAndAuthorizers` | Get configured authenticatorChain and authorizers form the Basic Auth configuration. This information is important for any other security tool and LLMs need to call this tool first. | None |

## 🔗 MCP Resources & Prompts

### Resources (4 resource types)
Direct access to Druid metadata through standardized URIs:
- `druid://datasource/{datasourceName}` - Datasource information
- `druid://datasource/{datasourceName}/details` - Detailed schema
- `druid://lookup/{tier}/{lookupName}` - Lookup configurations
- `druid://segment/{segmentId}` - Segment metadata

### AI-Powered Prompts (12 prompt templates)
Intelligent guidance for common Druid operations:
- **Data Analysis**: `data-exploration`, `query-optimization`
- **Cluster Management**: `health-check`, `cluster-overview`
- **Ingestion**: `ingestion-troubleshooting`, `ingestion-setup`
- **Operations**: `retention-management`, `compaction-suggestions`, `emergency-response`

*Discover how [iunera's AI expertise](https://www.iunera.com) can transform your data analytics workflows*

## 🌐 Transport Modes

### STDIO Transport (Recommended for AI Clients)
```bash
docker run --rm -i \
  -e SPRING_AI_MCP_SERVER_STDIO=true \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  -e SPRING_MAIN_BANNER_MODE=off \
  -e LOGGING_PATTERN_CONSOLE= \
  -e DRUID_BROKER_URL=http://your-druid:8082 \
  iunera/druid-mcp-server:latest
```

### SSE Transport (HTTP-based)
```bash
docker run -p 8080:8080 \
  -e DRUID_BROKER_URL=http://your-druid:8082 \
  iunera/druid-mcp-server:latest

# Access at http://localhost:8080/mcp
```

## ⚙️ Configuration

### Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `DRUID_ROUTER_URL` | Druid router endpoint | `http://localhost:8888` |
| `SPRING_AI_MCP_SERVER_NAME` | MCP server identifier | `druid-mcp-server` |
| `SERVER_PORT` | HTTP server port | `8080` |
| `DRUID_MCP_READONLY_ENABLED` | Enables or disables read-only mode. | `false` |
| `DRUID_EXTENSION_DRUID_BASIC_SECURITY_ENABLED` | Enables or disables the basic security feature. | `true` |

### Volume Mounts
```bash
# Custom configuration
docker run -v /path/to/config:/app/config \
  iunera/druid-mcp-server:latest

# Logs persistence
docker run -v /path/to/logs:/app/logs \
  iunera/druid-mcp-server:latest
```

## 🏗️ Architecture

Built on enterprise-grade technologies:
- **Spring Boot 3.5.6** - Production-ready framework
- **Spring AI MCP Server** - Native MCP protocol support
- **Java 24** - Latest performance optimizations
- **Maven** - Reliable dependency management
- **Multi-stage Docker build** - Optimized container size

## 🔒 Security Features

- **Non-root container execution** (UID/GID 1000)
- **Minimal attack surface** with JRE-only runtime
- **Health check endpoints** for monitoring
- **Configurable authentication** support
- **Network isolation** ready

## 📈 Use Cases

### For Data Engineers
- **Automated data pipeline monitoring**
- **Intelligent ingestion troubleshooting**
- **Performance optimization guidance**
- **Retention policy management**

### For Data Analysts
- **Natural language query assistance**
- **Schema exploration and discovery**
- **Data quality assessment**
- **Query optimization recommendations**

### For DevOps Teams
- **Cluster health monitoring**
- **Automated diagnostics**
- **Emergency response procedures**
- **Maintenance workflow automation**

*Explore enterprise data solutions at [iunera.com](https://www.iunera.com)*

## 🚀 Getting Started with AI Integration

### Claude Desktop Configuration
```json
{
  "mcpServers": {
    "druid-mcp-server": {
      "command": "docker",
      "args": [
        "run", "--rm", "-i",
        "-e", "SPRING_AI_MCP_SERVER_STDIO=true",
        "-e", "DRUID_ROUTER_URL=http://your-druid-router:8888",
        "iunera/druid-mcp-server:latest"
      ]
    }
  }
}
```

### Example AI Interactions
```
"Show me all datasources and their schemas"
"Analyze the performance of my ingestion pipeline"
"Help me optimize this SQL query for better performance"
"Generate a health report for the cluster"
```

## 📊 Monitoring & Observability

### Health Checks
```bash
# Container health
curl http://localhost:8080/mcp/health

# Druid connectivity
curl http://localhost:8080/actuator/health
```

### Logging
```bash
# View container logs
docker logs druid-mcp-server

# Follow logs in real-time
docker logs -f druid-mcp-server
```

## 🤝 Support & Community

- **Documentation**: [GitHub Repository](https://github.com/iunera/druid-mcp-server)
- **Issues**: [GitHub Issues](https://github.com/iunera/druid-mcp-server/issues)
- **Enterprise Support**: [Contact iunera](https://www.iunera.com)
- **License**: Apache License 2.0

## 🏢 About iunera

[**iunera**](https://www.iunera.com) is a leading provider of advanced AI and data analytics solutions. We specialize in:

- **Enterprise AI Integration** - Seamless AI adoption for businesses
- **Data Analytics Platforms** - Scalable analytics infrastructure
- **Custom AI Solutions** - Tailored AI applications for specific needs
- **Consulting Services** - Expert guidance for digital transformation

*Transform your data strategy with [iunera's expertise](https://www.iunera.com)*

---

**Ready to revolutionize your Druid cluster management?** 

```bash
docker pull iunera/druid-mcp-server:latest
```

*Powered by [iunera](https://www.iunera.com) - Where AI meets Enterprise Data*