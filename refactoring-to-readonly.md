### Objective
Implement a robust read-only mode in the Druid MCP server that hides all non-read-only tools when `druid.mcp.readonly.enabled=true`, and adopts a consistent naming and packaging scheme with one class per tool.

---

### What’s already in place (baseline)
- Property `druid.mcp.readonly.enabled` exists and is mapped to `ReadonlyModeProperties` (`@ConfigurationProperties(prefix = "druid.mcp.readonly")`). See `application.properties` and `ReadonlyModeProperties`.
- A working reference for the desired pattern already exists for compaction tools:
    - Read-only: `ReadCompactionConfigTools` (always active, `@Component`) with tools like `viewAllCompactionConfigs`, `viewCompactionStatus`, etc.
    - Write-only: `WriteCompactionConfigTools` (conditionally active, `@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)` + `@Component`).
- Tests for compaction have been adapted: `CompactionConfigIntegrationTest` validates behavior.

We will replicate and generalize this pattern across the entire project.

---

### High-level plan
1. Enforce Read/Write split and naming:
    - All read-only tools live in classes starting with `Read` and ending with `Tools`.
    - All write-capable tools live in classes starting with `Write` and ending with `Tools`.
    - Each MCP tool method gets its own dedicated class (one tool per class) inside the relevant feature package.
    - All `*ToolProvider` names are renamed to `*Tools` and then split further into one class per method/tool.

2. Bean activation rules:
    - Read tool classes: `@Component` only (always available in both modes).
    - Write tool classes: `@Component` plus `@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)` so they disappear in read-only mode.

3. Tests:
    - Add two integration test suites per feature area:
        - Read-only mode ON: ensure only read tools exist and write tools are absent.
        - Read-only mode OFF (default): ensure both read and write tools are present.
    - Use `@TestPropertySource(properties = {"druid.mcp.readonly.enabled=true"})` for the read-only variant.
    - Verify MCP tool registration or simply assert bean presence/absence by type and that tool methods respond properly. Include `[DEBUG_LOG]` outputs per guideline.

4. Tool exposure:
    - Only the following methods are exposed in read-only mode (all must be implemented in `Read*Tools` classes):
        - Compaction: `viewCompactionStatusForDatasource`, `viewCompactionStatus`, `viewCompactionConfigHistory`, `viewCompactionConfigForDatasource`, `viewAllCompactionConfigs`
        - Lookups: `listLookups`, `getLookupsForTier`, `getLookup`, `getLookupStatus`, `getLookupStatusForTier`
        - SQL: `queryDruidSql`
        - Retention: `viewAllRetentionRules`, `viewRetentionRuleHistory`
        - Segments: `listAllSegments`, `getSegmentsForDatasource`, `getSegmentsWithDetailsForDatasource`, `getSegmentDetails`, `getSegmentMetadata`, `getSegmentMetadataForDatasource`
        - Load queue: `getLoadQueueStatus`, `getLoadQueueStatusForServer`
        - Supervisors/Tasks: `listSupervisors`, `getSupervisorStatus`, `getTaskRawDetails`, `getTaskIngestionSpec`, `getTaskReports`, `getTaskLog`, `getTaskLogWithOffset`, `getTaskStatus`, `listRunningTasks`, `listPendingTasks`, `listWaitingTasks`
        - Health and cluster info: `checkClusterHealth`, `getCoordinatorHealth`, `getRouterHealth`, `getCoordinatorSelfDiscovered`, `getRouterSelfDiscovered`, `getAllServersStatusWithDetails`, `getServerStatus`, `getClusterMetadata`, `getLeaderInfo`, `isCoordinatorLeader`, `getCoordinatorProperties`, `getBrokerStatus`
        - Diagnostics & functionality: `diagnoseCluster`, `quickHealthCheck`, `analyzePerformance`, `validateConfiguration`, `checkSupervisorHealth`, `checkHistoricalHealth`, `checkFunctionalityHealth`, `quickFunctionalityCheck`

5. Package-by-feature consistency:
    - Keep read/write tools in the existing feature packages next to their `Repository` collaborators to preserve cohesion, e.g.:
        - `datamanagement/compaction`: `ReadCompactionConfigTools`, `WriteCompactionConfigTools` (already done) and further splits if needed.
        - `datamanagement/lookups`: read classes per lookup tool.
        - `datamanagement/retention`: read classes per retention tool.
        - `datamanagement/segments`: read classes per segment tool.
        - `ingestion/supervisors`, `ingestion/tasks`: read classes per supervisor/task tool.
        - `monitoring/health/repository` and `monitoring/health/*`: read classes for health checks and diagnostics.
        - `monitoring/cluster`, `monitoring/coordinator`, `monitoring/router`, `monitoring/broker`: read classes per cluster/leader properties tool.
        - `query/sql`: read class for `queryDruidSql`.

6. Documentation and prompts:
    - Update method-level `@McpTool(description = "...")` docstrings to clearly mark read-only semantics.
    - If you are using prompt templates or the `mcp-servers-config.json`, ensure tool names remain stable for clients; if any name changes, communicate or alias.

---

### Detailed steps and examples

#### 1) Refactor existing multi-tool classes into one-class-per-tool
- Example: `monitoring/health/diagnostics/DruidDoctorToolProvider` currently exposes multiple methods.
    - Split into separate read classes, one per method, keeping them in the same package:
        - `ReadDiagnoseClusterTools` with method `diagnoseCluster`
        - `ReadQuickHealthCheckTools` with method `quickHealthCheck`
        - `ReadAnalyzePerformanceTools` with method `analyzePerformance`
        - `ReadValidateConfigurationTools` with method `validateConfiguration`
    - Remove the old `DruidDoctorToolProvider` (or transitionally deprecate it) once clients are updated.

- Example: `monitoring/health/functionality/FunctionalityHealthToolProvider`
    - Split into:
        - `ReadCheckSupervisorHealthTools` with `checkSupervisorHealth`
        - `ReadCheckHistoricalHealthTools` with `checkHistoricalHealth`
        - `ReadCheckFunctionalityHealthTools` with `checkFunctionalityHealth`
        - `ReadQuickFunctionalityCheckTools` with `quickFunctionalityCheck`

- Apply similar splitting to any other class that currently groups multiple tools.

Notes:
- Constructor-inject the same `Repository` beans into each new class; Spring will deduplicate singletons.
- Keep `@Component` on each read class.
- Do not use conditional annotations on read classes so they’re available in both modes.

#### 2) Ensure write tools are gated by the property
- Follow the compaction example already present in `WriteCompactionConfigTools`:
```java
@Component
@ConditionalOnProperty(
    prefix = "druid.mcp.readonly",
    name = "enabled",
    havingValue = "false",
    matchIfMissing = true
)
public class WriteSomethingTools {
    @McpTool(description = "Writes something mutable in Druid ...")
    public String doWriteAction(String input) {
        // implementation
    }
}
```
- If any other write-capable tools exist (e.g., creating/deleting lookups, altering retention, triggering tasks), move each into its own `Write*Tools` class with the same conditional.

#### 3) Create read-only tool classes for the full allowed list
Below is a suggested mapping of class names (one per tool) and feature packages. Use the actual existing `Repository` types in those packages.

- Compaction (already partly implemented):
    - `datamanagement.compaction.ReadViewAllCompactionConfigsTools#viewAllCompactionConfigs`
    - `datamanagement.compaction.ReadViewCompactionConfigForDatasourceTools#viewCompactionConfigForDatasource`
    - `datamanagement.compaction.ReadViewCompactionConfigHistoryTools#viewCompactionConfigHistory`
    - `datamanagement.compaction.ReadViewCompactionStatusTools#viewCompactionStatus`
    - `datamanagement.compaction.ReadViewCompactionStatusForDatasourceTools#viewCompactionStatusForDatasource`

- Lookups:
    - `datamanagement.lookups.ReadListLookupsTools#listLookups`
    - `datamanagement.lookups.ReadGetLookupsForTierTools#getLookupsForTier`
    - `datamanagement.lookups.ReadGetLookupTools#getLookup`
    - `datamanagement.lookups.ReadGetLookupStatusTools#getLookupStatus`
    - `datamanagement.lookups.ReadGetLookupStatusForTierTools#getLookupStatusForTier`

- SQL:
    - `query.sql.ReadQueryDruidSqlTools#queryDruidSql`

- Retention:
    - `datamanagement.retention.ReadViewAllRetentionRulesTools#viewAllRetentionRules`
    - `datamanagement.retention.ReadViewRetentionRuleHistoryTools#viewRetentionRuleHistory`

- Segments:
    - `datamanagement.segments.ReadListAllSegmentsTools#listAllSegments`
    - `datamanagement.segments.ReadGetSegmentsForDatasourceTools#getSegmentsForDatasource`
    - `datamanagement.segments.ReadGetSegmentsWithDetailsForDatasourceTools#getSegmentsWithDetailsForDatasource`
    - `datamanagement.segments.ReadGetSegmentDetailsTools#getSegmentDetails`
    - `datamanagement.segments.ReadGetSegmentMetadataTools#getSegmentMetadata`
    - `datamanagement.segments.ReadGetSegmentMetadataForDatasourceTools#getSegmentMetadataForDatasource`

- Load queue:
    - `monitoring.loadqueue.ReadGetLoadQueueStatusTools#getLoadQueueStatus`
    - `monitoring.loadqueue.ReadGetLoadQueueStatusForServerTools#getLoadQueueStatusForServer`

- Supervisors/Tasks:
    - `ingestion.supervisors.ReadListSupervisorsTools#listSupervisors`
    - `ingestion.supervisors.ReadGetSupervisorStatusTools#getSupervisorStatus`
    - `ingestion.tasks.ReadGetTaskRawDetailsTools#getTaskRawDetails`
    - `ingestion.tasks.ReadGetTaskIngestionSpecTools#getTaskIngestionSpec`
    - `ingestion.tasks.ReadGetTaskReportsTools#getTaskReports`
    - `ingestion.tasks.ReadGetTaskLogTools#getTaskLog`
    - `ingestion.tasks.ReadGetTaskLogWithOffsetTools#getTaskLogWithOffset`
    - `ingestion.tasks.ReadGetTaskStatusTools#getTaskStatus`
    - `ingestion.tasks.ReadListRunningTasksTools#listRunningTasks`
    - `ingestion.tasks.ReadListPendingTasksTools#listPendingTasks`
    - `ingestion.tasks.ReadListWaitingTasksTools#listWaitingTasks`

- Health/Cluster/Server/Leader/Properties:
    - `monitoring.health.cluster.ReadCheckClusterHealthTools#checkClusterHealth`
    - `monitoring.health.coordinator.ReadGetCoordinatorHealthTools#getCoordinatorHealth`
    - `monitoring.health.router.ReadGetRouterHealthTools#getRouterHealth`
    - `monitoring.health.coordinator.ReadGetCoordinatorSelfDiscoveredTools#getCoordinatorSelfDiscovered`
    - `monitoring.health.router.ReadGetRouterSelfDiscoveredTools#getRouterSelfDiscovered`
    - `monitoring.health.server.ReadGetAllServersStatusWithDetailsTools#getAllServersStatusWithDetails`
    - `monitoring.health.server.ReadGetServerStatusTools#getServerStatus`
    - `monitoring.health.cluster.ReadGetClusterMetadataTools#getClusterMetadata`
    - `monitoring.health.cluster.ReadGetLeaderInfoTools#getLeaderInfo`
    - `monitoring.health.coordinator.ReadIsCoordinatorLeaderTools#isCoordinatorLeader`
    - `monitoring.health.coordinator.ReadGetCoordinatorPropertiesTools#getCoordinatorProperties`
    - `monitoring.health.broker.ReadGetBrokerStatusTools#getBrokerStatus`

- Diagnostics and functionality health (split from existing providers):
    - `monitoring.health.diagnostics.ReadDiagnoseClusterTools#diagnoseCluster`
    - `monitoring.health.diagnostics.ReadQuickHealthCheckTools#quickHealthCheck`
    - `monitoring.health.diagnostics.ReadAnalyzePerformanceTools#analyzePerformance`
    - `monitoring.health.diagnostics.ReadValidateConfigurationTools#validateConfiguration`
    - `monitoring.health.functionality.ReadCheckSupervisorHealthTools#checkSupervisorHealth`
    - `monitoring.health.functionality.ReadCheckHistoricalHealthTools#checkHistoricalHealth`
    - `monitoring.health.functionality.ReadCheckFunctionalityHealthTools#checkFunctionalityHealth`
    - `monitoring.health.functionality.ReadQuickFunctionalityCheckTools#quickFunctionalityCheck`

Each class should:
- Inject the appropriate `Repository` dependencies and `ObjectMapper` as needed (see compaction examples).
- Marshal `JsonNode` to `String` via `objectMapper.writeValueAsString(result)` and catch `RestClientException` plus generic `Exception` to return clear error messages.
- Use `@McpTool(description = "...")` on the single public tool method.

#### 4) Example read-only tool class template
```java
package com.iunera.druidmcpserver.monitoring.health.cluster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class ReadGetClusterMetadataTools {
    private final ClusterRepository clusterRepository;
    private final ObjectMapper objectMapper;

    public ReadGetClusterMetadataTools(ClusterRepository clusterRepository, ObjectMapper objectMapper) {
        this.clusterRepository = clusterRepository;
        this.objectMapper = objectMapper;
    }

    @McpTool(description = "Get Druid cluster metadata and basic state information.")
    public String getClusterMetadata() {
        try {
            JsonNode result = clusterRepository.getClusterMetadata();
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error retrieving cluster metadata: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process cluster metadata response: %s", e.getMessage());
        }
    }
}
```

#### 5) Example write-only tool class template
```java
package com.iunera.druidmcpserver.datamanagement.lookups;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(prefix = "druid.mcp.readonly", name = "enabled", havingValue = "false", matchIfMissing = true)
public class WriteUpdateLookupTools {
    private final LookupsRepository lookupsRepository;
    private final ObjectMapper objectMapper;

    public WriteUpdateLookupTools(LookupsRepository lookupsRepository, ObjectMapper objectMapper) {
        this.lookupsRepository = lookupsRepository;
        this.objectMapper = objectMapper;
    }

    @McpTool(description = "Update a lookup definition. Requires write access; hidden in read-only mode.")
    public String updateLookup(String tier, String name, String definitionJson) {
        try {
            JsonNode result = lookupsRepository.updateLookup(tier, name, definitionJson);
            return objectMapper.writeValueAsString(result);
        } catch (RestClientException e) {
            return String.format("Error updating lookup '%s' in tier '%s': %s", name, tier, e.getMessage());
        } catch (Exception e) {
            return String.format("Failed to process lookup update for '%s' in tier '%s': %s", name, tier, e.getMessage());
        }
    }
}
```

#### 6) Testing plan
Create or update tests in `src/test/java/com/iunera/druidmcpserver/...` per feature. Follow guidelines: `@SpringBootTest`, `@TestPropertySource`, `[DEBUG_LOG]` logging.

- Read-only mode ON test (example):
```java
@SpringBootTest
@TestPropertySource(properties = {"druid.mcp.readonly.enabled=true"})
class ReadonlyModeEnabledTests {

    @Autowired(required = false)
    private WriteCompactionConfigTools writeCompactionConfigTools;

    @Autowired
    private ReadCompactionConfigTools readCompactionConfigTools;

    @Test
    void writeBeansAreNotLoaded() {
        System.out.println("[DEBUG_LOG] Verifying write beans are absent in readonly mode");
        assertNull(writeCompactionConfigTools);
    }

    @Test
    void readBeansAreLoadedAndWork() {
        System.out.println("[DEBUG_LOG] Verifying read beans are present in readonly mode");
        assertNotNull(readCompactionConfigTools);
        String result = readCompactionConfigTools.viewAllCompactionConfigs();
        assertNotNull(result);
    }
}
```

- Read-only mode OFF test (example):
```java
@SpringBootTest
@TestPropertySource(properties = {"druid.mcp.readonly.enabled=false"})
class ReadonlyModeDisabledTests {

    @Autowired
    private WriteCompactionConfigTools writeCompactionConfigTools;

    @Autowired
    private ReadCompactionConfigTools readCompactionConfigTools;

    @Test
    void bothReadAndWriteBeansAreLoaded() {
        System.out.println("[DEBUG_LOG] Verifying both read and write beans are present when readonly is disabled");
        assertNotNull(writeCompactionConfigTools);
        assertNotNull(readCompactionConfigTools);
    }
}
```

- Per-feature smoke tests: For each new `Read*Tools` class, add a minimal test that calls the method and checks response is non-null and error handling path returns a friendly message if Druid is unavailable.

Optional:
- An actuator-based test to list MCP tool registrations (if you expose such endpoint) and assert only the allowed tool names exist in read-only mode.

#### 7) Migration and deprecation path
- Rename legacy `*ToolProvider` to `*Tools` and then split per-tool. For a short transition, you could:
    - Keep old classes annotated with `@Deprecated` and no `@Component`, to allow compile but not bean registration.
    - Update any internal references or documentation.

#### 8) Error handling and return types
- Follow existing pattern: return `String` JSON payloads, with consistent error messages. Always catch `RestClientException` explicitly and a generic `Exception` with contextual messages.

---

### Acceptance criteria checklist
- Property toggle works:
    - `druid.mcp.readonly.enabled=true`: no `Write*Tools` beans in the context; all `Read*Tools` beans present.
    - `druid.mcp.readonly.enabled=false` or missing: both read and write beans present (write conditional uses `matchIfMissing=true`).
- All tools in the provided allowed list are implemented as `Read*Tools`, one class per tool in the correct feature packages.
- All existing write-capable tools are moved into `Write*Tools` classes with the conditional annotation.
- All previous `*ToolProvider` classes are renamed to `*Tools` and split accordingly.
- Tests cover both modes and include `[DEBUG_LOG]` outputs. Tests verify bean presence/absence and basic call paths.
- No change is required to `ReadonlyModeProperties` or the property key names; they already match the compaction pattern and `application.properties`.

---

### Notes and tips
- Mirror the compaction implementation (`ReadCompactionConfigTools` and `WriteCompactionConfigTools`) as the canonical pattern.
- When splitting classes, keep constructor injection and avoid field injection; Spring will manage singletons correctly.
- To prevent method name collisions in MCP discovery, ensure each `@McpTool` method name is unique within its class. If you decide to use the `name` attribute on `@McpTool`, standardize names to match the allowed list exactly (e.g., `@McpTool(name = "viewCompactionStatus")`).
- If you later add write-capable versions of any currently read tool, ensure the write version lives in a `Write*Tools` class with the conditional and that tool names don’t clash.

This plan aligns with your compaction example, the read-only toggle, the class naming scheme, and the “one class per tool” requirement, while keeping packages organized by feature and ensuring tests prove the mode gating works end-to-end.