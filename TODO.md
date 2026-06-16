# combine tools 
  - all ingestion stuff in 3-4 tools
  - 1 tool for lookup
  - 1 tool for datasource
  - kill all "details" tools and add make difference between parametered toolcalls or not
  - 1-2 tools for health

# refactor profiles
We want to have the following profiles with enabled tools
  - query only - list datasources, lookups, compaction, query etc. 
  - cluster administration 
  - user management. when we enable basic security all other profiles are disabled. We need to instantiate twice
  - health invesitagation
  - we want to be able to define tools per datasource (limit datasources)


# Spring 4 
Replace the DruidRestClient Config by Spring-boot 4 httpexchange



What that composability unlocks in 2.0:
- Memory advisors can sit inside the tool loop and persist the full tool transcript, or outside for the safe default that works with any backend
- ToolSearchToolCallingAdvisor - graduated into core - one property enables progressive tool disclosure for any LLM
- Local @Tool methods and remote MCP server tools share the same ToolCallback interface — mix freely
- Drop in a custom ToolAdvisor for approval gates, budget caps, or domain-specific tool resolution; auto-configuration wires it transparently





https://www.linkedin.com/pulse/spring-ai-recipe-controlling-mcp-tool-visibility-craig-walls-tjbfc/

we replace the readonly mode by a tool filtering. We want to use https://www.linkedin.com/pulse/spring-ai-recipe-controlling-mcp-tool-visibility-craig-walls-tjbfc/
technique. 
the idea is that we define profile and in this profile we define the tools we want to enable or not. advice me first and get me an implemetation plan. If we do that we get rid of the @ConditionalOnProperty annotations in the Writer* classes. 
