# Druid MCP Server Development Guidelines

## Build/Configuration Instructions

### Prerequisites
- **Java 24** (main project) / **Java 17+** (mcp-annotations-server submodule)
- **Maven 3.6+**
- **Apache Druid cluster** (for integration testing with real Druid instance)

### Project Structure
This is a single-module Maven project with one main component:
- **Main module**: `druid-mcp-server` - The core Druid MCP server implementation

### Example Project for Juni

There is a submodule as example project for Juni. This project must not be changed.
- **Submodule**: `mcp-annotations-server` - Sample/reference implementation using MCP annotations

### Build Commands

#### Clean Build (Recommended)
```bash
mvn clean package -DskipTests
```

#### Build with Tests
```bash
mvn clean package
```

#### Build Specific Module
```bash
# Main module only
mvn clean package -DskipTests

# Annotations server submodule only
cd mcp-annotations-server
./mvnw clean package -DskipTests
```

### Key Dependencies
- **Spring Boot**: 3.5.3 (main), 3.3.6 (submodule)
- **Spring AI MCP Server**: 1.1.0-SNAPSHOT
- **Spring AI BOM**: 1.1.0-SNAPSHOT
- **MCP Annotations**: 0.1.0 (logaritex.mcp)

### Repository Configuration
The project requires custom Maven repositories for Spring AI snapshots:
- Spring Milestones: `https://repo.spring.io/milestone`
- Spring Snapshots: `https://repo.spring.io/snapshot`
- Central Portal Snapshots: `https://central.sonatype.com/repository/maven-snapshots/`

### Configuration Properties
Essential configuration in `application.properties`:
```properties
# MCP Server identification
spring.ai.mcp.server.name=druid-mcp-server
spring.ai.mcp.server.version=0.0.1-SNAPSHOT

# Druid connection
druid.broker.url=http://localhost:8082
druid.coordinator.url=http://localhost:8081

# Transport configuration
server.port=8080

# STDIO transport requirements
spring.main.banner-mode=off  # Required for STDIO
logging.pattern.console=     # Required for STDIO
```

### Autowiring
Features like Tools, Resources, Prompts and more, need to be autowired in the main Class `DruidMcpServerApplication.java` in the  `@Bean ToolCallbackProvider druidTools`

## Testing Information

### Test Structure
The project uses JUnit 5 with Spring Boot Test framework:
- **Unit Tests**: Basic functionality and configuration testing
- **Integration Tests**: Full Spring context with MCP service testing
- **Client Tests**: MCP client implementations for testing server functionality
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
mvn test -Dtest=SimpleTestExample
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

1. **Create test class** in `src/test/java/com/iunera/druidmcpserver/`
2. **Use appropriate annotations**: `@SpringBootTest`, `@TestPropertySource`
3. **Include debug logging**: Use `[DEBUG_LOG]` prefix for debugging
4. **Test both success and error scenarios**: Handle cases where Druid is not available
5. **Verify return types**: Ensure MCP tools return correct data types

### Example Test Creation
See `SimpleTestExample.java` for a complete example demonstrating:
- Configuration bean testing
- Property injection verification
- Debug logging usage
- Basic assertion patterns

## Additional Development Information

### MCP Architecture
The project implements Model Context Protocol (MCP) server with:
- **Tools**: Executable functions (`@Tool` annotation)
- **Resources**: Data providers (`@McpResource` annotation)
- **Transport**: STDIO and SSE support
- **Prompts**: Prompt templates (`@McpPrompt`annotation)
- **Autocomplete**: Autocomplete with Sampling (`@McpComplete` annotation)

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
     -jar target/druid-mcp-server-0.0.1-SNAPSHOT.jar
```

#### SSE Transport (HTTP-based)
```bash
java -jar target/druid-mcp-server-0.0.1-SNAPSHOT.jar
# Server available at http://localhost:8080
```

### Code Style Guidelines

#### Annotations Usage
- Use `@Tool` for MCP tool methods that return String
- Use `@McpResource` for resource methods that return `ReadResourceResult`
- Include descriptive method documentation for MCP tool discovery

#### Package by Feature 
- Always create a package by Feature structure to separate the concerns

#### Error Handling
- Always handle Druid connection failures gracefully
- Return meaningful error messages in JSON format
- Log errors with appropriate levels

#### Testing Patterns
- Use `[DEBUG_LOG]` prefix for all debug output in tests
- Test both success and failure scenarios
- Verify dependency injection with `assertNotNull()`
- Use `@TestPropertySource` for test-specific configuration

### Docker Integration
The project includes Docker support:
- `Dockerfile`: Container configuration


### MCP Client Configuration
Ready-to-use MCP configuration in `mcp-servers-config.json` for LLM clients supporting both STDIO and SSE transports.

### Debugging Tips
1. **Enable debug logging** in tests using `[DEBUG_LOG]` prefix
2. **Check Druid connectivity** before running integration tests
3. **Verify MCP tool registration** through Spring Boot actuator endpoints
4. **Test both transport modes** (STDIO/SSE) for compatibility
5. **Monitor application logs** in `target/druid-mcp-server.log`

### Common Issues
- **STDIO transport**: Requires banner and console logging disabled
- **Snapshot dependencies**: May require repository updates for latest versions
- **Druid connectivity**: Integration tests handle connection failures gracefully
- **Java version compatibility**: Main project uses Java 24, submodule uses Java 17+
