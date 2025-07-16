# Druid MCP Server

A comprehensive Model Context Protocol (MCP) server for Apache Druid that provides extensive tools, resources, and prompts for managing and analyzing Druid clusters.

*Developed by [iunera](https://www.iunera.com) - Advanced AI and Data Analytics Solutions*

## Overview

This MCP server implements a feature-based architecture where each package represents a distinct functional area of Druid management. The server provides three main types of MCP components:

- **Tools** - Executable functions for performing operations
- **Resources** - Data providers for accessing information  
- **Prompts** - AI-assisted guidance templates

## Video Walkthrough

Learn how to integrate AI agents with Apache Druid using the MCP server. This tutorial demonstrates time series data exploration, statistical analysis, and data ingestion using natural language with AI assistants like Claude, ChatGPT, and Gemini.

[![Time Series on AI Steroids: Apache Druid Enterprise MCP Server Tutorial](https://img.youtube.com/vi/BqCEWRZbRjU/0.jpg)](https://www.youtube.com/watch?v=BqCEWRZbRjU)

*Click the thumbnail above to watch the video on YouTube*

## Features

- Spring AI MCP Server integration
- Tool-based architecture for MCP protocol compliance
- STDIO and SSE transport support
- Comprehensive error handling
- Customizable prompt templates
- Feature-based package organization


### MCP Inspector Interface

When connected to an MCP client, you can inspect the available tools, resources, and prompts through the MCP inspector interface:

#### Available Tools
![MCP Inspector - Tools](assets/images/mcpinspector-tools.png)

The tools interface shows all available Druid management functions organized by feature areas including data management, ingestion management, and monitoring & health.

#### Available Resources
![MCP Inspector - Resources](assets/images/mcpinspector-resources.png)

The resources interface displays all accessible Druid data sources and metadata that can be retrieved through the MCP protocol.

#### Available Prompts
![MCP Inspector - Prompts](assets/images/mcpinspector-prompts.png)

The prompts interface shows all AI-assisted guidance templates available for various Druid management tasks and data analysis workflows.




## Quick Start

### Prerequisites
- Java 24
- Maven 3.6+
- Apache Druid cluster running with router on port 8888

### Build and Run
```bash
# Build the application
mvn clean package -DskipTests

# Run the application
java -jar target/druid-mcp-server-1.0.0.jar
```

The server will start on port 8080 by default.

For detailed build instructions, testing, Docker setup, and development guidelines, see [development.md](development.md).

## Installation from Maven Central

If you prefer to use the pre-built JAR without building from source, you can download and run it directly from Maven Central.

### Prerequisites
- Java 24 JRE only

### Download and Run

```bash
# Create a directory for the application
mkdir druid-mcp-server && cd druid-mcp-server

# Download the JAR from Maven Central
curl -L -o druid-mcp-server-1.0.0.jar \
  "https://repo.maven.apache.org/maven2/com/iunera/druid-mcp-server/1.0.0/druid-mcp-server-1.0.0.jar"

# Run with SSE Transport (HTTP-based, default)
java -jar druid-mcp-server-1.0.0.jar

# OR run with STDIO Transport (recommended for LLM clients)
java -Dspring.ai.mcp.server.stdio=true \
     -Dspring.main.web-application-type=none \
     -Dlogging.pattern.console= \
     -jar druid-mcp-server-1.0.0.jar
```

## For Developers

For detailed development information including build instructions, testing guidelines, architecture details, and contributing guidelines, see [development.md](development.md).

## Available Tools by Feature

### Data Management

| Feature | Tool | Description | Parameters |
|---------|------|-------------|------------|
| **Datasource** | `listDatasources` | List all available Druid datasource names | None |
| **Datasource** | `showDatasourceDetails` | Show detailed information for a specific datasource including column information | `datasourceName` (String) |
| **Datasource** | `killDatasource` | Kill a datasource permanently, removing all data and metadata | `datasourceName` (String), `interval` (String) |
| **Lookup** | `listLookups` | List all available Druid lookups from the coordinator | None |
| **Lookup** | `getLookupConfig` | Get configuration for a specific lookup | `tier` (String), `lookupName` (String) |
| **Lookup** | `updateLookupConfig` | Update configuration for a specific lookup | `tier` (String), `lookupName` (String), `config` (String) |
| **Segments** | `listAllSegments` | List all segments across all datasources | None |
| **Segments** | `getSegmentMetadata` | Get metadata for specific segments | `datasourceName` (String), `segmentId` (String) |
| **Segments** | `getSegmentsForDatasource` | Get all segments for a specific datasource | `datasourceName` (String) |
| **Query** | `queryDruidSql` | Execute a SQL query against Druid datasources | `sqlQuery` (String) |
| **Retention** | `viewRetentionRules` | View retention rules for all datasources or a specific one | `datasourceName` (String, optional) |
| **Retention** | `updateRetentionRules` | Update retention rules for a datasource | `datasourceName` (String), `rules` (String) |
| **Compaction** | `viewAllCompactionConfigs` | View compaction configurations for all datasources | None |
| **Compaction** | `viewCompactionConfigForDatasource` | View compaction configuration for a specific datasource | `datasourceName` (String) |
| **Compaction** | `editCompactionConfigForDatasource` | Edit compaction configuration for a datasource | `datasourceName` (String), `config` (String) |
| **Compaction** | `deleteCompactionConfigForDatasource` | Delete compaction configuration for a datasource | `datasourceName` (String) |
| **Compaction** | `viewCompactionStatus` | View compaction status for all datasources | None |
| **Compaction** | `viewCompactionStatusForDatasource` | View compaction status for a specific datasource | `datasourceName` (String) |

### Ingestion Management

| Feature | Tool | Description | Parameters |
|---------|------|-------------|------------|
| **Ingestion Spec** | `createBatchIngestionTemplate` | Create a batch ingestion template | `datasourceName` (String), `inputSource` (String), `timestampColumn` (String) |
| **Ingestion Spec** | `createIngestionSpec` | Create and submit an ingestion specification | `specJson` (String) |
| **Supervisors** | `listSupervisors` | List all streaming ingestion supervisors | None |
| **Supervisors** | `getSupervisorStatus` | Get status of a specific supervisor | `supervisorId` (String) |
| **Supervisors** | `suspendSupervisor` | Suspend a streaming supervisor | `supervisorId` (String) |
| **Supervisors** | `startSupervisor` | Start or resume a streaming supervisor | `supervisorId` (String) |
| **Supervisors** | `terminateSupervisor` | Terminate a streaming supervisor | `supervisorId` (String) |
| **Tasks** | `listTasks` | List all ingestion tasks | None |
| **Tasks** | `getTaskStatus` | Get status of a specific task | `taskId` (String) |
| **Tasks** | `shutdownTask` | Shutdown a running task | `taskId` (String) |

### Monitoring & Health

| Feature | Tool | Description | Parameters |
|---------|------|-------------|------------|
| **Basic Health** | `checkClusterHealth` | Check overall cluster health status | None |
| **Basic Health** | `getServiceStatus` | Get status of specific Druid services | `serviceType` (String) |
| **Basic Health** | `getClusterConfiguration` | Get cluster configuration information | None |
| **Diagnostics** | `runDruidDoctor` | Run comprehensive cluster diagnostics | None |
| **Diagnostics** | `analyzePerformanceIssues` | Analyze cluster performance issues | None |
| **Diagnostics** | `generateHealthReport` | Generate detailed health report | None |
| **Functionality** | `testQueryFunctionality` | Test query functionality across services | None |
| **Functionality** | `testIngestionFunctionality` | Test ingestion functionality | None |
| **Functionality** | `validateClusterConnectivity` | Validate connectivity between cluster components | None |

## Available Resources by Feature

| Feature | Resource URI Pattern | Description | Parameters |
|---------|---------------------|-------------|------------|
| **Datasource** | `druid://datasource/{datasourceName}` | Access datasource information and metadata | `datasourceName` (String) |
| **Datasource** | `druid://datasource/{datasourceName}/details` | Access detailed datasource information including schema | `datasourceName` (String) |
| **Lookup** | `druid://lookup/{tier}/{lookupName}` | Access lookup configuration and data | `tier` (String), `lookupName` (String) |
| **Segments** | `druid://segment/{segmentId}` | Access segment metadata and information | `segmentId` (String) |

## Available Prompts by Feature

| Feature | Prompt Name | Description | Parameters |
|---------|-------------|-------------|------------|
| **Data Analysis** | `data-exploration` | Guide for exploring data in Druid datasources | `datasource` (String, optional) |
| **Data Analysis** | `query-optimization` | Help optimize Druid SQL queries for better performance | `query` (String) |
| **Cluster Management** | `health-check` | Comprehensive cluster health assessment guidance | None |
| **Cluster Management** | `cluster-overview` | Overview and analysis of cluster status | None |
| **Ingestion Management** | `ingestion-troubleshooting` | Troubleshoot ingestion issues | `issue` (String, optional) |
| **Ingestion Management** | `ingestion-setup` | Guide for setting up new ingestion pipelines | `dataSource` (String, optional) |
| **Retention Management** | `retention-management` | Manage data retention policies | `datasource` (String, optional) |
| **Compaction** | `compaction-suggestions` | Optimize segment compaction configuration | `datasource` (String, optional), `currentConfig` (String, optional), `performanceMetrics` (String, optional) |
| **Compaction** | `compaction-troubleshooting` | Troubleshoot compaction issues | `issue` (String), `datasource` (String, optional) |
| **Operations** | `emergency-response` | Emergency response procedures and guidance | None |
| **Operations** | `maintenance-mode` | Cluster maintenance procedures | None |

## Configuration

Configure your Druid connection in `src/main/resources/application.properties`:

```properties
# Spring AI MCP Server configuration
spring.ai.mcp.server.name=druid-mcp-server
spring.ai.mcp.server.version=1.0.0

# Druid configuration
druid.router.url=http://localhost:8888

# Server configuration
server.port=8080

# NOTE: Banner and console logging must be disabled for STDIO transport
spring.main.banner-mode=off
```

### Environment Variables Configuration

For sensitive credentials like username and password, you can use environment variables instead of hardcoding them in properties files.

#### Supported Environment Variables

- `DRUID_AUTH_USERNAME`: Druid authentication username
- `DRUID_AUTH_PASSWORD`: Druid authentication password  
- `DRUID_ROUTER_URL`: Override the default Druid router URL
- `DRUID_SSL_ENABLED`: Enable SSL/TLS support (true/false)
- `DRUID_SSL_SKIP_VERIFICATION`: Skip SSL certificate verification (true/false)

### SSL-Encrypted Cluster with Authentication

This section provides comprehensive guidance on connecting to SSL-encrypted Druid clusters with username and password authentication.

#### Prerequisites

- SSL-enabled Druid cluster with HTTPS endpoints
- Valid username and password credentials for Druid authentication
- SSL certificates properly configured (or ability to skip verification for testing)

#### Configuration Methods

##### Method 1: Environment Variables (Recommended for Production)

Set the following environment variables before starting the MCP server:

```bash
# Druid cluster URL with HTTPS
export DRUID_ROUTER_URL="https://your-druid-cluster.example.com:8888"

# Authentication credentials
export DRUID_AUTH_USERNAME="your-username"
export DRUID_AUTH_PASSWORD="your-password"

# SSL configuration
export DRUID_SSL_ENABLED="true"
export DRUID_SSL_SKIP_VERIFICATION="false"  # Use "true" only for testing

# Start the MCP server
java -jar target/druid-mcp-server-1.0.0.jar
```

##### Method 2: Application Properties

Update `src/main/resources/application.properties`:

```properties
# Druid cluster configuration
druid.router.url=https://your-druid-cluster.example.com:8888

# Authentication
druid.auth.username=your-username
druid.auth.password=your-password

# SSL configuration
druid.ssl.enabled=true
druid.ssl.skip-verification=false
```

##### Method 3: Runtime System Properties

Pass configuration as JVM system properties:

```bash
java -Ddruid.router.url="https://your-druid-cluster.example.com:8888" \
     -Ddruid.auth.username="your-username" \
     -Ddruid.auth.password="your-password" \
     -Ddruid.ssl.enabled=true \
     -Ddruid.ssl.skip-verification=false \
     -jar target/druid-mcp-server-1.0.0.jar
```

#### SSL Configuration Options

##### Production SSL Setup

For production environments with valid SSL certificates:

```bash
export DRUID_ROUTER_URL="https://druid-prod.company.com:8888"
export DRUID_SSL_ENABLED="true"
export DRUID_SSL_SKIP_VERIFICATION="false"
```

The server will use the system's default truststore to validate SSL certificates.

##### Development/Testing SSL Setup

For development or testing with self-signed certificates:

```bash
export DRUID_ROUTER_URL="https://druid-dev.local:8888"
export DRUID_SSL_ENABLED="true"
export DRUID_SSL_SKIP_VERIFICATION="true"  # WARNING: Only for testing!
```

**⚠️ Security Warning**: Never use `DRUID_SSL_SKIP_VERIFICATION=true` in production environments as it disables SSL certificate validation.

#### Authentication Methods

The MCP server supports HTTP Basic Authentication with username and password:

- **Username**: Set via `DRUID_AUTH_USERNAME` or `druid.auth.username`
- **Password**: Set via `DRUID_AUTH_PASSWORD` or `druid.auth.password`

The credentials are automatically encoded using Base64 and sent with each request using the `Authorization: Basic` header.

#### Complete Example Configurations

##### Example 1: Production Environment

```bash
#!/bin/bash
# Production configuration script

# Druid cluster settings
export DRUID_ROUTER_URL="https://druid.production.company.com:8888"

# Authentication
export DRUID_AUTH_USERNAME="druid-mcp-user"
export DRUID_AUTH_PASSWORD="secure-password-123"

# SSL settings (production)
export DRUID_SSL_ENABLED="true"
export DRUID_SSL_SKIP_VERIFICATION="false"

# Start MCP server
java -jar target/druid-mcp-server-1.0.0.jar
```

##### Example 2: Development Environment

```bash
#!/bin/bash
# Development configuration script

# Local Druid cluster with self-signed certificates
export DRUID_ROUTER_URL="https://localhost:8888"

# Test credentials
export DRUID_AUTH_USERNAME="admin"
export DRUID_AUTH_PASSWORD="admin123"

# SSL settings (development - skip verification)
export DRUID_SSL_ENABLED="true"
export DRUID_SSL_SKIP_VERIFICATION="true"

# Start MCP server
java -jar target/druid-mcp-server-1.0.0.jar
```

##### Example 3: MCP Client Configuration with SSL

Update your `mcp-servers-config.json` to include environment variables:

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
      ],
      "env": {
        "DRUID_ROUTER_URL": "https://your-druid-cluster.example.com:8888",
        "DRUID_AUTH_USERNAME": "your-username",
        "DRUID_AUTH_PASSWORD": "your-password",
        "DRUID_SSL_ENABLED": "true",
        "DRUID_SSL_SKIP_VERIFICATION": "false"
      }
    }
  }
}
```

## MCP Prompt Customization

The server provides extensive prompt customization capabilities through the `prompts.properties` file located in `src/main/resources/`.

### Prompt Configuration Structure

The prompts.properties file contains:

1. **Global Settings**: Enable/disable prompts and set watermarks
2. **Feature Toggles**: Control which prompts are available
3. **Custom Variables**: Organization-specific information
4. **Template Definitions**: Full prompt templates for each feature

### Overriding Prompts

You can override any prompt template using Java system properties with the `-D` flag:

#### Method 1: System Properties (Runtime Override)

```bash
java -Dprompts.druid-data-exploration.template="Your custom template here" \
     -jar target/druid-mcp-server-1.0.0.jar
```

#### Method 2: Custom Properties File

1. Create a custom properties file (e.g., `custom-prompts.properties`):
```properties
# Custom prompt template
prompts.druid-data-exploration.template=My custom data exploration prompt:\n\
1. Custom step one\n\
2. Custom step two\n\
{datasource_section}\n\
Environment: {environment}
```

2. Load it at runtime:
```bash
java -Dspring.config.additional-location=classpath:custom-prompts.properties \
     -jar target/druid-mcp-server-1.0.0.jar
```

### Available Prompt Variables

All prompt templates support these variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `{environment}` | Current environment name | `production`, `staging`, `dev` |
| `{organizationName}` | Organization name | `Your Organization` |
| `{contactInfo}` | Contact information | `your-team@company.com` |
| `{watermark}` | Generated watermark | `Generated by Druid MCP Server v1.0.0` |
| `{datasource}` | Datasource name (context-specific) | `sales_data` |
| `{query}` | SQL query (context-specific) | `SELECT * FROM sales_data` |

### Prompt Template Examples

#### Custom Data Exploration Prompt
```properties
prompts.druid-data-exploration.template=Welcome to {organizationName} Druid Analysis!\n\n\
Please help me explore our data:\n\
{datasource_section}\n\
Environment: {environment}\n\
Contact: {contactInfo}\n\n\
{watermark}
```

#### Custom Query Optimization Prompt
```properties
prompts.druid-query-optimization.template=Query Performance Analysis for {organizationName}\n\n\
Query to optimize: {query}\n\n\
Please provide:\n\
1. Performance bottleneck analysis\n\
2. Optimization recommendations\n\
3. Best practices for our {environment} environment\n\n\
{watermark}
```

### Disabling Specific Prompts

You can disable individual prompts by setting their enabled flag to false:

```properties
mcp.prompts.data-exploration.enabled=false
mcp.prompts.query-optimization.enabled=false
```

Or disable all prompts globally:
```properties
mcp.prompts.enabled=false
```

## MCP Integration

This server uses Spring AI's MCP Server framework and supports both STDIO and SSE transports. The tools, resources, and prompts are automatically registered and exposed through the MCP protocol.

### Transport Modes

#### STDIO Transport (Recommended for LLM clients)
```bash
java -Dspring.ai.mcp.server.stdio=true \
     -Dspring.main.web-application-type=none \
     -Dlogging.pattern.console= \
     -jar target/druid-mcp-server-1.0.0.jar
```

#### SSE Transport (HTTP-based)
```bash
java -jar target/druid-mcp-server-1.0.0.jar
# Server available at http://localhost:8080
```

### MCP Configuration for LLMs

A ready-to-use MCP configuration file is provided at `mcp-servers-config.json` that can be used with LLM clients to connect to this Druid MCP server. 

The configuration includes both transport options:

#### STDIO Transport (Recommended)
More details on this on [examples/stdio/README.md](examples/stdio/README.md).

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

#### SSE Transport
More details on this on [examples/sse/README.md](examples/stdio/README.md).

```json
{
  "mcpServers": {
    "druid-mcp-server-sse": {
      "url": "http://localhost:8080"
    }
  }
}
```

### Using with LLM Clients

1. **Build the server first:** See [development.md](development.md) for build instructions
2. **For STDIO transport:** The MCP server will be automatically started by the LLM client
3. **For SSE transport:** Start the server manually first

## Examples

This repository includes comprehensive examples to help you get started with different deployment scenarios and transport modes:

### 🐳 [Druid Cluster Setup](examples/druidcluster/README.md)
Complete Docker Compose configuration for running a full Apache Druid cluster locally. Perfect for development, testing, and learning about Druid cluster architecture.

**Features:**
- Full Druid cluster with all components (Coordinator, Broker, Historical, MiddleManager, Router)
- PostgreSQL metadata storage and ZooKeeper coordination
- Pre-configured with sample data and ingestion examples
- Integrated Druid MCP Server for immediate testing

### 📡 [STDIO Transport Configuration](examples/stdio/README.md)
Configuration examples for STDIO (Standard Input/Output) transport mode - the recommended method for integrating with LLM clients like Claude Desktop.

**Features:**
- Development and production configuration templates
- Authentication and SSL setup examples
- Integration guides for popular MCP clients
- Troubleshooting and security best practices

### 🌐 [SSE Transport Configuration](examples/sse/README.md)
Configuration examples for SSE (Server-Sent Events) transport mode, providing HTTP-based communication suitable for web applications and REST API integrations.

**Features:**
- HTTP-based MCP server configuration
- Custom port and production deployment examples
- Web client integration patterns
- Comparison with STDIO transport mode


## Related Projects

This Druid MCP Server is part of a comprehensive ecosystem of Apache Druid tools and extensions developed by iunera. These complementary projects enhance different aspects of Druid cluster management and data ingestion:

### 🔧 [Druid Cluster Configuration](https://github.com/iunera/druid-cluster-config)
Advanced configuration management and deployment tools for Apache Druid clusters. This project provides:

- **Automated Cluster Setup**: Streamlined configuration templates for different deployment scenarios
- **Configuration Management**: Best practices and templates for production Druid clusters
- **Deployment Automation**: Tools and scripts for consistent cluster deployments
- **Environment-Specific Configs**: Optimized configurations for development, staging, and production environments

**Integration with Druid MCP Server**: The cluster configurations provided by this project work seamlessly with the monitoring and management capabilities of the Druid MCP Server, enabling comprehensive cluster lifecycle management.

### 📊 [Code Ingestion Druid Extension](https://github.com/iunera/iu-code-ingestion-druid-extension)
A specialized Apache Druid extension for ingesting and analyzing code-related data and metrics. This extension enables:

- **Code Metrics Ingestion**: Specialized parsers for code analysis data and software metrics
- **Developer Analytics**: Tools for analyzing code quality, complexity, and development patterns
- **CI/CD Integration**: Seamless integration with continuous integration and deployment pipelines
- **Custom Data Formats**: Support for various code analysis tools and formats

**Integration with Druid MCP Server**: This extension expands the ingestion capabilities that can be managed through the MCP server's ingestion management tools, providing specialized support for code analytics use cases.

### Why Use These Together?

- **Complete Ecosystem**: From cluster setup to specialized data ingestion and management
- **Consistent Architecture**: All projects follow similar design principles and integration patterns
- **Enhanced Capabilities**: Each project extends different aspects of the Druid ecosystem
- **Production Ready**: Battle-tested configurations and extensions for enterprise deployments

## Roadmap

- **Readonly Mode**: Implement a Readonly Mode (R) for Druid and disallow the Create, Update, Delete on all tools.
- **Authentication on SSE Mode**: Introduce Oauth Authentication
- **Druid Auto Compaction**: Intelligent automatic compaction configuration
- **MCP Auto Completion**: Enhanced autocomplete functionality with sampling
- **Proper Observability**: Comprehensive metrics and tracing
- **Enhanced Monitoring**: Advanced cluster monitoring and alerting capabilities
- **Advanced Analytics**: Machine learning-powered insights and recommendations
- **Security Enhancements**: Advanced authentication and authorization features
- **Kubernetes Support**: Proper deployment on Kubernetes

---

## About iunera

This Druid MCP Server is developed and maintained by **[iunera](https://www.iunera.com)**, a leading provider of advanced AI and data analytics solutions. 

iunera specializes in:
- **AI-Powered Analytics**: Cutting-edge artificial intelligence solutions for data analysis
- **Enterprise Data Platforms**: Scalable data infrastructure and analytics platforms (Druid, Flink, Kubernetes, Kafka, Spring)
- **Model Context Protocol (MCP) Solutions**: Advanced MCP server implementations for various data systems
- **Custom AI Development**: Tailored AI solutions for enterprise needs

As veterans in Apache Druid iunera deployed and maintained a large number of solutions based on [Apache Druid](https://druid.apache.org/) in productive enterprise grade scenarios. 

For more information about our services and solutions, visit [www.iunera.com](https://www.iunera.com).

### Contact & Support

Need help? Let 

- **Website**: [https://www.iunera.com](https://www.iunera.com)
- **Professional Services**: Contact us through [www.iunera.com](https://www.iunera.com) or [email](mailto:contact@iunera.com?subject=Druid%20MCP%20Server%20inquiry) for enterprise support and custom development
- **Open Source**: This project is open source and community contributions are welcome

---

*© 2024 [iunera](https://www.iunera.com). Licensed under the Apache License 2.0.*
