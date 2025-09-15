### MCP Annotations Are The Standard Here — Use @McpTool (and friends)

This document corrects the previous refactoring note. In this project we standardize on the MCP Annotations programming model for MCP servers. That means:

- Use @McpTool for Tools
- Use @McpResource for Resources
- Use @McpPrompt for Prompts
- Use @McpComplete for Autocomplete

Why: MCP Annotations provide first-class, annotation-driven support for MCP with automatic JSON schema generation, special parameter injection, and consistent discovery/registration across Tools, Resources, Prompts, and Completion.

References for verification:
- Spring AI Reference (1.1.0-SNAPSHOT) – MCP Annotations overview: https://docs.spring.io/spring-ai/reference/1.1-SNAPSHOT/api/mcp/mcp-annotations-overview.html
- Server annotations list shows @McpTool, @McpResource, @McpPrompt, @McpComplete
- Community repo: https://github.com/spring-ai-community/mcp-annotations
- Examples: https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/mcp-annotations


### What changed vs. the previous note

The previous document incorrectly claimed that @McpTool does not exist and advised removing the MCP annotations dependency. That is outdated. The current Spring AI docs show @McpTool as part of the MCP Annotations module. In this codebase we already use MCP annotations for Resources and Prompts and should align Tools as well.


### Recommended patterns for this repository

We support both patterns below, but we recommend (and will migrate to) the MCP Annotations model end‑to‑end.

1) MCP Annotations – recommended end‑to‑end approach
- Annotations: @McpTool, @McpResource, @McpPrompt, @McpComplete
- Registration: use the MCP annotations scanner from Spring AI’s MCP Boot starters or use an annotation provider utility to build the specifications.
- Benefits:
  - Automatic JSON schema generation for tool parameters via @McpToolParam
  - Access to special parameters/tokens (e.g., McpSyncServerExchange, McpAsyncServerExchange, McpTransportContext, @McpProgressToken, McpMeta)
  - Unified, declarative MCP programming model across server features

Example (@McpTool):
```java
import org.springframework.stereotype.Component;
import org.springframework.ai.mcp.annotations.server.McpTool;       // package per Spring AI MCP annotations
import org.springframework.ai.mcp.annotations.server.McpToolParam;  // parameter metadata

@Component
public class QueryTools {

    @McpTool(name = "query-druid-sql", description = "Execute a SQL query against Druid datasources")
    public String queryDruidSql(
            @McpToolParam(description = "SQL query string", required = true) String sqlQuery) {
        // implementation
        return "...";
    }
}
```

Example (@McpResource already used in this repo):
```java
import org.springframework.stereotype.Service;
import org.springframework.ai.mcp.annotations.server.McpResource;

@Service
public class DatasourceResources {

    @McpResource(uri = "datasource://{datasourcename}", name = "Datasource",
                 description = "Provides basic information for a specific Druid datasource")
    public ReadResourceResult getDatasource(ReadResourceRequest request, String datasourcename) {
        // implementation
    }
}
```

Boot auto-scanning (preferred when using MCP Server Boot starter):
```properties
# enabled by default when using the MCP server boot starters
spring.ai.mcp.server.annotation-scanner.enabled=true
```

2) Spring AI @Tool + MethodToolCallbackProvider – supported but legacy for this repo
- Annotations: org.springframework.ai.tool.annotation.Tool
- Registration: MethodToolCallbackProvider with ToolCallbackProvider bean
- Status here: still supported by our code, but we aim to migrate tools to @McpTool for consistency with resources/prompts and to leverage schema generation + special parameters.

Current wiring example (still valid):
```java
@Bean
public ToolCallbackProvider druidTools(QueryToolProvider query) {
    return MethodToolCallbackProvider.builder().toolObjects(query).build();
}
```


### What to do in this repository

- Keep MCP annotations and related configuration. Do not remove them.
- New tools should use @McpTool and @McpToolParam.
- Existing @Tool methods are acceptable short‑term, but plan to migrate them to @McpTool.
- Resources continue to use @McpResource and Prompts use @McpPrompt.
- For every Resource, also expose a dedicated Tool if you want LLMs to invoke it directly (per our project guidelines).

Migration steps from @Tool to @McpTool:
1. Replace imports:
   - From: org.springframework.ai.tool.annotation.Tool
   - To:   org.springframework.ai.mcp.annotations.server.McpTool
2. Convert method parameters to annotated @McpToolParam where appropriate to enrich schema and descriptions.
3. If you use special server features (logging/progress/state), inject the appropriate special parameter types (e.g., McpSyncServerExchange) and/or @McpProgressToken.
4. If using Boot starters, prefer the built‑in MCP annotation scanner to avoid manual registration; otherwise continue using the helper provider to build specifications for prompts/resources and ToolCallbackProvider for any remaining @Tool methods during migration.


### Dependency guidance

- If you target Spring AI 1.1.0‑SNAPSHOT (recommended for MCP Annotations), include the official Spring AI MCP Annotations module (pulled transitively by the MCP Server Boot starter). See: https://docs.spring.io/spring-ai/reference/1.1-SNAPSHOT/api/mcp/mcp-annotations-overview.html
- If you remain on older milestones/releases, the community MCP annotations package can be used. The API names are the same conceptually (@McpTool, @McpResource, etc.).
- This project’s repository configuration already enables Spring snapshots and milestones. Ensure your local Maven uses them for SNAPSHOT builds when you move to 1.1.0‑SNAPSHOT.


### Context7 note (keywords corrected)

When searching external documentation through Context7 or similar integrations, prefer the following keywords for best results:
- “Spring AI MCP Annotations”
- “@McpTool” “@McpResource” “@McpPrompt” “@McpComplete”
- “MCP annotation scanner spring.ai.mcp.server.annotation-scanner.enabled”
- “Spring AI MCP Server Boot Starter”


### Bottom line

- In this project, @McpTool is the standard for new tools. We will migrate existing @Tool usages to @McpTool.
- Do not remove MCP annotations usage; align Tools, Resources, and Prompts under the same MCP annotations model.
- Use Boot auto‑scanning where possible; otherwise keep the existing registration code during the transition.

