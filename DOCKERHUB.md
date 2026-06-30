# Druid MCP Server

[![Docker Image](https://img.shields.io/badge/docker-available-blue.svg)](https://hub.docker.com/r/iunera/druid-mcp-server)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-green.svg)](https://spring.io/projects/spring-boot)

A comprehensive **Model Context Protocol (MCP) server** for Apache Druid that provides extensive tools, resources, and AI-assisted prompts for managing and analyzing Druid clusters. Built with enterprise-grade reliability and performance in mind.

*Developed by [**iunera**](https://www.iunera.com) - Your trusted partner for Advanced AI and Data Analytics Solutions*

## 🚀 Quick Start

### Pull and Run
```bash
# Pull the latest image
docker pull iunera/druid-mcp-server:latest

# STDIO mode (default, recommended for LLM clients that spawn the server)
docker run --rm -i \
  -e DRUID_ROUTER_URL=http://your-druid-router:8888 \
  -e SPRING_PROFILES_ACTIVE=query \
  iunera/druid-mcp-server:latest

# HTTP mode (enable profile 'http' and expose /mcp)
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=http,query \
  -e DRUID_ROUTER_URL=http://your-druid-router:8888 \
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
      - SPRING_PROFILES_ACTIVE=http,query
      - DRUID_ROUTER_URL=http://druid-router:8888
      - SPRING_AI_MCP_SERVER_NAME=druid-mcp-server
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/"]
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

## 🌊 Y̊pipe: AI-Powered UI for Druid

Experience your data like never before with **[Y̊pipe](https://ypipe.com)**, a local desktop application that makes offline AI practical, designed by iunera. It leverages this **Druid MCP Server** to provide a seamless, conversational interface for your Druid cluster.

*   **Natural Language Queries:** Ask questions in plain English and get results instantly.
*   **Local & Secure:** Runs completely locally with support for offline models (CPU/GPU).
*   **Plug & Play:** Works out-of-the-box with the Development Druid Installation.

[**Get Ypipe on GitHub →**](https://github.com/iunera/ypipe)

*The easiest way to test iunera/druid-mcp-server is [ypipe.com](https://ypipe.com) / [https://github.com/iunera/ypipe](https://github.com/iunera/ypipe)*

## 🛠️ Comprehensive Tools Summary

The MCP server activates tools dynamically based on active Spring profiles (`SPRING_PROFILES_ACTIVE`).

### 📊 Data Management & Querying Tools (6 tools - `query` profile)
Perfect for data exploration, schema analysis, and standard querying:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `getDatasources` | List analytical tables or get detailed schema schemas | `datasourceName` (optional), `detailed` (optional) |
| `getLookups` | Retrieve lookup configurations and tier statuses | `tier` (optional), `lookupName` (optional), `includeStatus` (optional) |
| `getSegments` | Fetch segments list or metadata details | `datasource` (optional), `segmentId` (optional), `detailed` (optional) |
| `getSegmentLoadQueue` | View segments currently loading or dropping | `serverName` (optional) |
| `getRetentionRules` | Retrieve data retention rules and rule history | `datasource` (optional), `includeHistory` (optional) |
| `queryDruidSql` | Execute standard synchronous analytical SQL SELECT queries | `sqlQuery` (required) |

### ⚙️ Cluster Administration Tools (10 tools - `ops` profile)
Perform administrative tasks, compaction management, and multi-stage queries:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `getCompactionConfig` | View compaction settings or change history | `datasource` (optional), `includeHistory` (optional) |
| `getCompactionStatus` | Monitor background compaction runs | `datasource` (optional) |
| `manageCompaction` | Create, update, or delete compaction rules | `action` (UPSERT/DELETE), `datasource`, `configJson` |
| `manageDatasourceOrSegment` | Drop datasources or enable/disable segments | `action`, `datasource`, `segmentId`, `interval` |
| `manageLookup` | Configure or delete tier lookups | `action` (UPSERT/DELETE), `tier`, `lookupName`, `configJson` |
| `manageRetentionRules` | Update load/drop policies for a datasource | `datasource`, `rulesJson` |
| `queryDruidMultiStage` | Run asynchronous Multi-Stage SQL tasks (MSQ) | `sqlQuery` |
| `queryDruidMultiStageWithContext` | Run MSQ tasks with custom context properties | `sqlQuery`, `contextJson` |
| `getMultiStageQueryTaskStatus` | Retrieve MSQ query execution status | `taskId` |
| `cancelMultiStageQueryTask` | Abort a running MSQ task | `taskId` |

### 📥 Ingestion Tools (6 tools - `ops` profile)
Control batch and streaming data ingestion:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `submitIngestion` | Post ingestion job specs or generate batch JSON templates | `action` (SUBMIT_SPEC/GENERATE_TEMPLATE), `payloadJson`, `datasourceName` |
| `getSupervisors` | List continuous streaming ingestion supervisors | `supervisorId` (optional) |
| `manageSupervisor` | Suspend, resume, or terminate supervisor streams | `supervisorId`, `action` (SUSPEND/RESUME/TERMINATE) |
| `getTasks` | List ingestion tasks | `state` (RUNNING/PENDING/WAITING/COMPLETED) |
| `getTaskDetails` | View specs, status, reports, or task logs | `taskId`, `aspect` (STATUS/RAW_DETAILS/SPEC/REPORTS/LOG) |
| `shutdownTask` | Stop a running ingestion task | `taskId` |

### 🏥 Monitoring & Diagnostics Tools (4 tools - `health` profile)
Validate query performance, configurations, and check system health:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `getClusterStatus` | Heartbeat status, leaders, metadata | `aspect` (OVERALL/COORDINATOR/ROUTER/LEADER/PROPERTIES) |
| `getNodesStatus` | List active server nodes status | `serverName` (optional), `detailed` (optional) |
| `diagnoseCluster` | COMPREHENSIVE, QUICK, PERFORMANCE, CONFIGuration audits | `mode` |
| `checkFunctionalityHealth` | Smoke-test latency and active stream pipelines | `component`, `quick` |

### 🔐 Basic Security Tools (3 tools - `permissions` profile)
Administer users, roles, and resource access policies:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `manageAuthentication` | Create/delete users, list users, set passwords | `authenticator`, `action` (LIST/GET/CREATE/DELETE/SET_PASSWORD), `username` |
| `manageAuthorization` | Manage roles, permissions, access policies | `authorizer`, `action` (LIST_USERS/GET_ROLE/SET_PERMISSIONS), `name`, `permissionsJson` |
| `manageSecurityAssignments` | Map roles to users, view authenticator chain | `authorizer`, `action` (ASSIGN_ROLE/UNASSIGN_ROLE/GET_CHAIN), `username`, `roleName` |

## 🔗 MCP Resources & Prompts

### Resources
Direct access to Druid metadata through standardized URIs:
- `druid://datasource/{datasourceName}` - Datasource schema information
- `druid://datasource/{datasourceName}/details` - Detailed segment data structures
- `druid://lookup/{tier}/{lookupName}` - Lookup configurations
- `druid://segment/{segmentId}` - Core segment catalog metadata

### AI-Powered Prompts
Intelligent templates for common Druid operations:
- **Data Analysis**: `data-exploration`, `query-optimization`
- **Cluster Management**: `health-check`, `cluster-overview`
- **Ingestion**: `ingestion-troubleshooting`, `ingestion-setup`
- **Operations**: `retention-management`, `compaction-suggestions`, `emergency-response`

*Discover how [iunera's AI expertise](https://www.iunera.com) can transform your data analytics workflows*

## 🌐 Transport Modes

### STDIO Transport (Recommended for AI Clients)
```bash
docker run --rm -i \
  -e DRUID_ROUTER_URL=http://your-druid-router:8888 \
  -e SPRING_PROFILES_ACTIVE=query \
  iunera/druid-mcp-server:latest
```

### HTTP Transport (Profile: http)
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=http,query \
  -e DRUID_ROUTER_URL=http://your-druid-router:8888 \
  iunera/druid-mcp-server:latest

# Streamable HTTP endpoint: http://localhost:8080/mcp
```

## ⚙️ Configuration

### Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `DRUID_ROUTER_URL` | Druid router endpoint | `http://localhost:8888` |
| `DRUID_COORDINATOR_URL` | Coordinator endpoint (optional, needed for basic security) | `http://localhost:8081` |
| `DRUID_AUTH_USERNAME` | Username for basic auth authentication | `""` |
| `DRUID_AUTH_PASSWORD` | Password for basic auth authentication | `""` |
| `DRUID_SSL_ENABLED` | Enables SSL for connection | `false` |
| `DRUID_SSL_SKIP_VERIFICATION` | Skip SSL validations | `false` |
| `DRUID_MCP_SQL_SYNTAX_CORRECTION_ENABLED` | Enables automatic SQL syntax correction | `true` |
| `DRUID_MCP_SQL_SYNTAX_CORRECTION_CACHE_TTL_MS`| Schema metadata cache TTL in ms | `300000` (5 minutes) |
| `SPRING_PROFILES_ACTIVE` | Active Spring tool profiles (`query`, `ops`, `health`, `permissions`) | `query` |
| `SERVER_PORT` | Port for HTTP transport mode | `8080` |

## 🏗️ Architecture

Built on enterprise-grade technologies:
- **Spring Boot 4.1.0** - Production-ready framework
- **Spring AI MCP Server** - Native MCP protocol support
- **Java 25** - Latest performance optimizations
- **Maven** - Reliable dependency management
- **Multi-stage Docker build** - Optimized container size

## 🔒 Security Features

- **Non-root container execution** (UID/GID 1000)
- **Minimal attack surface** with JRE-only runtime
- **Health check endpoints** for monitoring
- **Configurable authentication** support
- **Network isolation** ready

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

### Need Expert Apache Druid Consulting?

**Maximize your return on data** with professional Druid implementation and optimization services. From architecture design to performance tuning and AI integration, our experts help you navigate Druid's complexity and unlock its full potential.

**[Get Expert Druid Consulting →](https://www.iunera.com/apache-druid-ai-consulting-europe/)**

### Need Enterprise MCP Server Development Consulting?

**ENTERPRISE AI INTEGRATION & CUSTOM MCP (MODEL CONTEXT PROTOCOL) SERVER DEVELOPMENT**

Iunera specializes in developing production-grade AI agents and enterprise-grade LLM solutions, helping businesses move beyond generic AI chatbots. They build secure, scalable, and future-ready AI infrastructure, underpinned by the Model Context Protocol (MCP), to connect proprietary data, legacy systems, and external APIs to advanced AI models.

**[Get Enterprise MCP Server Development Consulting →](https://www.iunera.com/enterprise-mcp-server-development/)**

---

**Ready to revolutionize your Druid cluster management?** 

```bash
docker pull iunera/druid-mcp-server:latest
```

*Powered by [iunera](https://www.iunera.com) - Where AI meets Enterprise Data*