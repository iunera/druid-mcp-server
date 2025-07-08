# Druid Cluster Docker Compose Example

This directory contains a complete Docker Compose setup for running a full Apache Druid cluster locally. This example is perfect for development, testing, and learning about Druid cluster architecture.

## Overview

This Docker Compose configuration sets up a complete Druid cluster with all necessary components:

- **PostgreSQL** - Metadata storage database
- **Apache ZooKeeper** - Coordination service
- **Druid Coordinator** - Manages data availability and segment lifecycle
- **Druid Broker** - Handles queries from external clients
- **Druid Historical** - Stores and serves historical data segments
- **Druid MiddleManager** - Manages ingestion tasks
- **Druid Router** - Routes queries and provides unified API endpoint
- **Druid MCP Server** - Model Context Protocol server for AI assistant integration

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │   ZooKeeper     │    │     Router      │
│ (Metadata DB)   │    │ (Coordination)  │    │   :8888 (UI)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┼─────────────┐
                                 │                       │             │
    ┌────────────────────────────┼────────────────────────────┐        │
    │                            │                            │        │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐        │
│   Coordinator   │    │     Broker      │    │   Historical    │        │
│ (Data Mgmt)     │    │   (Queries)     │    │ (Data Storage)  │        │
└─────────────────┘    └─────────────────┘    └─────────────────┘        │
                                 │                                       │
                       ┌─────────────────┐              ┌─────────────────┐
                       │ MiddleManager   │              │ MCP Server SSE  │
                       │  (Ingestion)    │              │  :8080 (API)    │
                       └─────────────────┘              └─────────────────┘
```

## Quick Start

### Prerequisites

- Docker and Docker Compose installed
- At least 8GB of available RAM
- At least 10GB of free disk space

### Starting the Cluster

1. Navigate to this directory:
   ```bash
   cd examples/druidcluster
   ```

2. Start the cluster:
   ```bash
   docker-compose up -d
   ```

3. Wait for all services to be healthy (this may take 2-3 minutes):
   ```bash
   docker-compose ps
   ```

4. Access the Druid Console at: http://localhost:8888

### Stopping the Cluster

```bash
docker-compose down
```

To also remove volumes (delete all data):
```bash
docker-compose down -v
```

## Service Details

### Core Services

| Service | Container | Image | Purpose | Dependencies |
|---------|-----------|-------|---------|--------------|
| **PostgreSQL** | `postgres` | `postgres:latest` | Metadata storage | None |
| **ZooKeeper** | `zookeeper` | `zookeeper:3.5.10` | Service coordination | None |
| **Coordinator** | `coordinator` | `apache/druid:33.0.0` | Segment management | postgres, zookeeper |
| **Broker** | `broker` | `apache/druid:33.0.0` | Query processing | postgres, zookeeper, coordinator |
| **Historical** | `historical` | `apache/druid:33.0.0` | Data serving | postgres, zookeeper, coordinator |
| **MiddleManager** | `middlemanager` | `apache/druid:33.0.0` | Task execution | postgres, zookeeper, coordinator |
| **Router** | `router` | `apache/druid:33.0.0` | API gateway | postgres, zookeeper, coordinator |
| **Druid MCP Server** | `druid-mcp-server` | `Built from source` | MCP server for Druid | router |

### Port Mappings

| Service | Internal Port | External Port | Purpose |
|---------|---------------|---------------|---------|
| Router | 8888 | 8888 | Druid Console & API |
| Druid MCP Server | 8080 | 8080 | MCP Server API |

### Volume Mappings

| Volume | Purpose | Mounted Services |
|--------|---------|------------------|
| `metadata_data` | PostgreSQL data persistence | postgres |
| `druid_shared` | Shared segment storage | coordinator, historical, middlemanager |
| `coordinator_var` | Coordinator logs and temp files | coordinator |
| `broker_var` | Broker logs and temp files | broker |
| `historical_var` | Historical logs and temp files | historical |
| `middle_var` | MiddleManager logs and temp files | middlemanager |
| `router_var` | Router logs and temp files | router |

## Configuration

### Environment Variables

The cluster uses configuration from the `environment` file. Key settings include:

#### Database Configuration
```bash
druid_metadata_storage_type=postgresql
druid_metadata_storage_connector_connectURI=jdbc:postgresql://postgres:5432/druid
druid_metadata_storage_connector_user=druid
druid_metadata_storage_connector_password=FoolishPassword
```

#### ZooKeeper Configuration
```bash
druid_zk_service_host=zookeeper
```

#### Storage Configuration
```bash
druid_storage_type=local
druid_storage_storageDirectory=/opt/shared/segments
druid_indexer_logs_directory=/opt/shared/indexing-logs
```

#### Performance Tuning
```bash
DRUID_SINGLE_NODE_CONF=micro-quickstart
druid_processing_numThreads=2
druid_processing_numMergeBuffers=2
```

### Customizing Configuration

To modify the cluster configuration:

1. Edit the `environment` file
2. Restart the cluster:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

## Druid MCP Server Integration

This cluster includes a **Druid MCP Server** that runs automatically alongside the Druid cluster. The MCP server provides a Model Context Protocol interface for interacting with Druid through AI assistants and other MCP-compatible clients.

### MCP Server Configuration

The MCP server is pre-configured with the following settings:

```yaml
druid-mcp-server:
  build: ../../
  container_name: druid-mcp-server
  environment:
    - druid.router.url=http://router:8888
    - server.port=8080
  ports:
    - "8080:8080"
  networks:
    - druid-network
  depends_on:
    - router
```

### MCP Server Features

The included MCP server provides:
- **Tools**: Execute Druid operations (queries, datasource management, etc.)
- **Resources**: Access Druid metadata and configuration
- **Prompts**: AI-assisted guidance for Druid operations

### Disabling the MCP Server

If you don't need the MCP server, you can disable it by commenting out the `druid-mcp-server` service in the `docker-compose.yaml` file:

```yaml
# druid-mcp-server:
#   build: ../../
#   container_name: druid-mcp-server
#   environment:
#     - druid.router.url=http://router:8888
#     - server.port=8080
#   ports:
#     - "8080:8080"
#   networks:
#     - druid-network
#   depends_on:
#     - router
```

## Accessing Services

### Druid Console
- **URL**: http://localhost:8888
- **Features**: Query interface, cluster monitoring, ingestion management

### Druid MCP Server
- **URL**: http://localhost:8080
- **Features**: Model Context Protocol interface for AI assistants
- **Health Check**: http://localhost:8080/actuator/health
- **Documentation**: See [main README](../../README.md) for MCP tools and usage

### Direct API Access
- **Router API**: http://localhost:8888/druid/v2/
- **SQL Endpoint**: http://localhost:8888/druid/v2/sql/

### Database Access
- **Host**: localhost:5432 (if PostgreSQL port is exposed)
- **Database**: druid
- **Username**: druid
- **Password**: FoolishPassword

## Troubleshooting

### Common Issues

#### Services Not Starting
```bash
# Check service logs
docker-compose logs [service-name]

# Check all services status
docker-compose ps
```

#### Out of Memory Errors
- Increase Docker memory allocation to at least 8GB
- Modify memory settings in the `environment` file

#### Port Conflicts
- Ensure ports 8888 (Druid Router) and 8080 (MCP Server) are not in use by other applications
- Modify port mappings in `docker-compose.yaml` if needed

#### Data Persistence Issues
```bash
# Remove all volumes and start fresh
docker-compose down -v
docker-compose up -d
```

### Health Checks

Check if all services are running:
```bash
# View running containers
docker-compose ps

# Check specific service logs
docker-compose logs coordinator
docker-compose logs broker
docker-compose logs druid-mcp-server

# Follow logs in real-time
docker-compose logs -f
```

### Performance Monitoring

Monitor resource usage:
```bash
# View resource usage
docker stats

# Check Druid metrics via API
curl http://localhost:8888/status/health

# Check MCP Server health
curl http://localhost:8080/actuator/health
```

## Sample Data Ingestion

Once the cluster is running, you can test it with sample data:

### Using the Druid Console
1. Go to http://localhost:8888
2. Click "Load data" → "Batch - classic"
3. Use the sample data provided in Druid tutorials

### Using SQL
```sql
-- Example: Create a simple datasource
INSERT INTO "sample_data" 
SELECT 
  TIME_PARSE('2023-01-01T00:00:00.000Z') AS __time,
  'test_value' AS dimension1,
  100 AS metric1
```

## Advanced Configuration

### Scaling Services

To run multiple instances of services, modify the `docker-compose.yaml`:

```yaml
historical:
  # ... existing configuration
  deploy:
    replicas: 2
```

### Custom Extensions

Add custom Druid extensions by modifying the `druid_extensions_loadList` in the `environment` file:

```bash
druid_extensions_loadList=["druid-histogram", "druid-datasketches", "druid-lookups-cached-global", "postgresql-metadata-storage", "druid-multi-stage-query", "your-custom-extension"]
```

### Security Configuration

For production use, update security settings:

1. Change default passwords in the `environment` file
2. Configure authentication and authorization
3. Enable TLS/SSL connections

## Cleanup

### Remove Everything
```bash
# Stop and remove containers, networks, and volumes
docker-compose down -v

# Remove images (optional)
docker rmi apache/druid:33.0.0 postgres:latest zookeeper:3.5.10
```

### Selective Cleanup
```bash
# Remove only containers and networks (keep volumes)
docker-compose down

# Remove specific volumes
docker volume rm druidcluster_metadata_data
```

## Resources

- [Apache Druid Documentation](https://druid.apache.org/docs/latest/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Druid MCP Server Documentation](../../README.md)

## Support

For issues specific to this Docker Compose setup, check:
1. Docker and Docker Compose versions
2. Available system resources (RAM, disk space)
3. Port availability
4. Service logs using `docker-compose logs`

For Druid-specific issues, consult the [Apache Druid documentation](https://druid.apache.org/docs/latest/) and community resources.

---

## About iunera

This Druid MCP Server and Docker Compose example is developed and maintained by **[iunera](https://www.iunera.com)**, a leading provider of advanced AI and data analytics solutions.

For more information about our enterprise solutions and professional services, visit [www.iunera.com](https://www.iunera.com).

---

*© 2024 [iunera](https://www.iunera.com). Licensed under the Apache License 2.0.*
