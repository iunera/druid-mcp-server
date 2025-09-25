# Druid MCP Server Development Guidelines

## Build/Configuration Instructions

### Prerequisites
- **Java 21** (main project)
- **Maven 3.6+**
- **Apache Druid cluster** (for integration testing with real Druid instance)

### Project Structure
This is a single-module Maven project with one main component:
- **Main module**: `druid-mcp-server` - The core Druid MCP server implementation

### Build Commands

#### Clean Build (Recommended)
```bash
mvn clean package -DskipTests
```

#### Build with Tests
```bash
mvn clean package
```

### Key Dependencies
- Spring Boot: 3.5.6 (main)
- Spring AI MCP Server: 1.1.0-M2 (with official MCP annotations support)
- Spring AI BOM: 1.1.0-M2
- Spring AI MCP Annotations: Included in Spring AI 1.1.0-M2

### Repository Configuration
The project requires custom Maven repositories for Spring AI milestones/snapshots:
- Spring Milestones: https://repo.spring.io/milestone
- Spring Snapshots: https://repo.spring.io/snapshot
- Central Portal Snapshots: https://central.sonatype.com/repository/maven-snapshots/

### Configuration Properties
Add these essentials to `src/main/resources/application.yaml` (or as env vars):
```yaml
# MCP Server identification
spring:
  ai:
    mcp:
      server:
        name: druid-mcp-server
        version: 1.2.2

# Druid connection
druid:
  router:
    url: http://localhost:8888

# Transport configuration
server:
  port: 8080

# STDIO transport requirements (only for stdio mode)
spring:
  main:
    banner-mode: off
logging:
  pattern:
    console:
```

### Autowiring
Features like Tools, Resources, Prompts and more are automatically discovered and registered by Spring AI 1.1.0-M1 auto-configuration through annotation scanning. Simply use `@Component` on classes with `@McpTool`, `@McpResource`, `@McpPrompt`, and `@McpComplete` annotated methods. No manual registration in the main application class is required.

## Version Management

### Updating Project Version

The project includes an automated version update script that synchronizes version numbers across all documentation and configuration files.

#### Version Update Script
The `scripts/update-version.sh` script updates version numbers in the following files:
- `pom.xml` (Maven project version)
- `src/main/resources/application.yaml` (MCP server version)
- `server.json` (MCP registry version and Docker image tag)
- `mcpservers-stdio.json` (Docker image tag)
- `README.md` (JAR file references)

#### Usage
```bash
# Update to a new version
./scripts/update-version.sh 1.3.0

# Update to a release candidate
./scripts/update-version.sh 2.0.0-RC1
```

#### Features
- **Validation**: Ensures version format follows semantic versioning (X.Y.Z or X.Y.Z-SUFFIX)
- **Confirmation**: Prompts for user confirmation before proceeding
- **Summary**: Shows detailed summary of all changes made

#### Workflow
1. Run the script with desired version number
2. Review the changes: `git diff`
3. Test the build: `mvn clean package`
4. Commit changes: `git add . && git commit -m "Update version to X.Y.Z"`
5. Push to trigger release: `git push origin main`

## Testing Information

### Test Structure
The project uses JUnit 5 with Spring Boot Test framework:
- Unit Tests: Basic functionality and configuration testing
- Integration Tests: Full Spring context with MCP service testing
- Client Tests: MCP client implementations for testing server functionality
- Create test classes for the corresponding implementation feature.

### Running Tests

#### All Tests
```bash
mvn test
```

#### Specific Test Classes
```bash
mvn test -Dtest=DruidMcpServerApplicationTests
mvn test -Dtest=DruidServicesIntegrationTest
```

#### Test with Debug Output
Tests include debug logging with `[DEBUG_LOG]` prefix:
```
System.out.println("[DEBUG_LOG] Your debug message here");
```

### Test Configuration Patterns

#### Basic Spring Boot Test
```
@SpringBootTest
class MyTest {
    @Test
    void contextLoads() {
        // Basic context loading test
    }
}
```

#### Integration Test with Custom Properties
```
@SpringBootTest
@TestPropertySource(properties = {
    "druid.broker.url=http://test-broker:8082",
    "druid.coordinator.url=http://test-coordinator:8081"
})
class MyIntegrationTest {
    @Autowired
    private DruidConfig druidConfig;

    @Test
    void testConfiguration() {
        assertNotNull(druidConfig.getDruidBrokerUrl());
    }
}
```

### Adding New Tests
1. Create test class in `src/test/java/com/iunera/druidmcpserver/`
2. Use appropriate annotations: `@SpringBootTest`, `@TestPropertySource`
3. Include debug logging: Use `[DEBUG_LOG]` prefix for debugging
4. Test both success and error scenarios: Handle cases where Druid is not available
5. Verify return types: Ensure MCP tools return correct data types

### Example Test Creation
See `SimpleTestExample.java` for a complete example demonstrating:
- Configuration bean testing
- Property injection verification
- Debug logging usage
- Basic assertion patterns

## Docker Integration
The project includes Docker support via `Dockerfile` for containerized runs.

The project includes Docker support with a `Dockerfile` and `docker-compose.yaml` for easy deployment.

### Docker Build
```bash
docker build -t druid-mcp-server .
```

### Docker Compose
```bash
docker-compose up -d
```

### Services Included
- **Druid Coordinator** (port 8081) - Manages data availability and replication
- **Druid Broker** (port 8082) - Handles queries from external clients
- **Druid Historical** (port 8083) - Stores queryable data
- **Druid MiddleManager** (port 8091) - Handles ingestion tasks
- **Druid Router** (port 8888) - Optional load balancer and unified entry point
- **Druid MCP Server** (port 8080) - Your application

### Accessing Services
Once all services are running:

- **MCP Server**: http://localhost:8080
- **Druid Router**: http://localhost:8888 (unified Druid UI)
- **Druid Coordinator**: http://localhost:8081
- **Druid Broker**: http://localhost:8082

## Architecture

### Feature-Based Organization
The project follows a feature-based package structure where each package represents a distinct functional area:

- **`datamanagement`** - Core data operations (datasources, segments, lookups, queries, retention, compaction)
- **`ingestion`** - Data ingestion management (specs, supervisors, tasks)
- **`monitoring`** - Cluster health and diagnostics (basic health, diagnostics, functionality testing)
- **`operations`** - Operational procedures and emergency response
- **`config`** - Configuration and shared services
- **`shared`** - Common utilities and components

### Component Types
Each feature may contain:
- ToolProvider - Implements `@McpTool` annotated methods for executable functions
- Resources - Implements `@McpResource` annotated methods for data access
- PromptProvider - Implements `@McpPrompt` annotated methods for AI guidance
- Repository - Data access layer for Druid APIs
- Configuration - Feature-specific configuration classes

### Project Structure
```
src/
├── main/java/com/iunera/druidmcpserver/
│   ├── DruidMcpServerApplication.java
│   ├── config/
│   │   ├── DruidConfig.java
│   │   └── PromptTemplateService.java
│   ├── datamanagement/
│   │   ├── compaction/
│   │   │   ├── ReadCompactionConfigTools.java
│   │   │   ├── WriteCompactionConfigTools.java
│   │   │   ├── CompactionConfigRepository.java
│   │   │   └── CompactionPrompts.java
│   │   ├── datasource/
│   │   │   └── [Datasource management components]
│   │   ├── lookup/
│   │   │   └── [Lookup management components]
│   │   ├── query/
│   │   │   └── [Query execution components]
│   │   ├── retention/
│   │   │   └── [Retention policy components]
│   │   └── segments/
│   │       └── [Segment management components]
│   ├── ingestion/
│   │   ├── IngestionManagementPrompts.java
│   │   ├── spec/
│   │   │   └── [Ingestion specification components]
│   │   ├── supervisors/
│   │   │   └── [Supervisor management components]
│   │   └── tasks/
│   │       └── [Task management components]
│   ├── monitoring/health/
│   │   ├── basic/
│   │   │   └── [Basic health check components]
│   │   ├── diagnostics/
│   │   │   └── [Diagnostic tools components]
│   │   ├── functionality/
│   │   │   └── [Functionality health components]
│   │   ├── prompts/
│   │   │   └── [Health monitoring prompts]
│   │   └── repository/
│   │       └── [Health data repositories]
│   ├── operations/
│   │   └── OperationalPrompts.java
│   ├── readonly/
│   │   └── ReadonlyModeProperties.java
│   │   └── ReadonlyRestClientInterceptor.java
│   └── resources/
│       └── [Configuration files and templates]
└── test/java/com/iunera/druidmcpserver/
    └── [Comprehensive test suite matching main structure]
```

### Key Technologies
- Spring Boot 3.5.6 - Main framework
- Spring AI MCP Server 1.1.0-M1 - MCP protocol implementation
- Jackson - JSON processing
- RestClient - HTTP client for Druid communication
- JUnit 5 - Testing framework

## MCP Architecture
The project implements Model Context Protocol (MCP) server with:
- Tools: Executable functions (`@McpTool` annotation)
- Resources: Data providers (`@McpResource` annotation)
- Transport: STDIO and SSE support
- Prompts: Prompt templates (`@McpPrompt` annotation)
- Autocomplete: Autocomplete with Sampling (`@McpComplete` annotation)

For every Resource we need a separate Tool to access it in addition.

### Key Components

#### Tool Providers
- `QueryService`: SQL query execution against Druid
- `DatasourceToolProvider`: Datasource listing functionality

#### Resource Providers
- `DatasourceResources`: MCP resource interface for datasource information

#### Configuration
- `DruidConfig`: Druid connection configuration with RestClient beans

### Transport Modes

#### STDIO Transport (Recommended for LLM clients)
```bash
java -Dspring.ai.mcp.server.stdio=true \
     -Dspring.main.web-application-type=none \
     -Dlogging.pattern.console= \
     -jar target/druid-mcp-server-1.2.2.jar
```

#### SSE Transport (HTTP-based)
```bash
java -jar target/druid-mcp-server-1.2.2.jar
# Server available at http://localhost:8080
```

## Error Handling

The application includes comprehensive error handling:

- Connection errors to Druid are caught and returned as error messages
- Tool execution errors are handled gracefully
- All tools return string responses with error details when operations fail
- Resource access errors provide meaningful feedback
- Prompt generation errors are handled with fallback templates

## Contributing
When adding new features:
1. Create a new package under the appropriate feature category (package-by-feature)
2. Implement ToolProvider for executable functions using `@McpTool` annotation
3. Add Resources if the feature provides data access using `@McpResource` annotation
4. Create PromptProvider for AI guidance using `@McpPrompt` annotation
5. Add Repository for Druid API interactions
6. Add tests following the existing patterns
7. Update documentation as needed

### Annotations Usage
- Use `@McpTool` for MCP tool methods (with optional `annotations` metadata)
- Use `@McpToolParam` to define required input parameters with descriptions
- Use `@McpResource` for resource methods that return `ReadResourceResult`
- Include descriptive method documentation for MCP tool discovery

#### Example (@McpTool)
```java
@Component
public class ExampleToolProvider {

    @McpTool(name = "query-druid-sql", description = "Execute a SQL query against Druid datasources")
    public String queryDruidSql(
            @McpToolParam(description = "SQL query string", required = true) String sqlQuery) {
        // Implementation
        return "result";
    }

    @McpTool(name = "calculate-area",
             description = "Calculate the area of a rectangle",
             annotations = @McpTool.McpAnnotations(
                 title = "Rectangle Area Calculator",
                 readOnlyHint = true,
                 destructiveHint = false,
                 idempotentHint = true
             ))
    public AreaResult calculateRectangleArea(
            @McpToolParam(description = "Width", required = true) double width,
            @McpToolParam(description = "Height", required = true) double height) {
        return new AreaResult(width * height, "square units");
    }
}
```

### Package by Feature Guidelines
- Always create a package-by-feature structure to separate concerns
- Each feature should be self-contained with its own tools, resources, and prompts
- Follow the established naming conventions for consistency

### Error Handling Guidelines
- Always handle Druid connection failures gracefully
- Return meaningful error messages in JSON format
- Log errors with appropriate levels

### Testing Patterns
- Use `[DEBUG_LOG]` prefix for all debug output in tests
- Test both success and failure scenarios
- Verify dependency injection with `assertNotNull()`
- Use `@TestPropertySource` for test-specific configuration

### MCP Client Configuration
Ready-to-use MCP configuration in `mcp-servers-config.json` for LLM clients supporting both STDIO and SSE transports.

### Debugging Tips
1. Enable debug logging in tests using `[DEBUG_LOG]` prefix
2. Check Druid connectivity before running integration tests
3. Verify MCP tool registration through Spring Boot actuator endpoints
4. Test both transport modes (STDIO/SSE) for compatibility
5. Monitor application logs in `target/druid-mcp-server.log`

### Common Issues
- STDIO transport: Requires banner and console logging disabled
- Snapshot/Milestone dependencies: May require repository updates for latest versions
- Druid connectivity: Integration tests should handle connection failures gracefully
- Java version compatibility: Main project uses Java 24, submodule uses Java 17+

---

## About iunera
This Druid MCP Server is developed and maintained by [iunera](https://www.iunera.com), a leading provider of advanced AI and data analytics solutions.

For more information about our enterprise solutions and professional services, visit https://www.iunera.com.

---

*© 2024 [iunera](https://www.iunera.com). Licensed under the Apache License 2.0.*
