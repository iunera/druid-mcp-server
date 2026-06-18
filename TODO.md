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


/Users/chris/git/github/iunera/druid-mcp-server/all_tools.txt many tools are expensive in llm tokens and context size. lets try to consolidate the tools. 
I have in mind that e.g. we combine tools like listDatasources and showDatasourceDetails because they are acutally the same. Also getting the segment data can be part of the datasources.

Furthermore the whole src/main/java/com/iunera/druidmcpserver/monitoring/health package can be combined in a handful of tools 

make a suggestions in an refactoring plan. 


lets get rid of the readonly property. This is legacy.  SecurityTools

secritytools werden dann aktiviert wenn der coordinator gesetzt ist. 

DRUID_MCP_READONLY_ENABLED

We still need to support the legacy readonly mode parameter druid.mcp.readonly.enabled: true
if this is set we only allow the activation of the following profiles 
http
stdio
query-only
Health-Investigation


Rework the ypipe integration blueprint [druid.ypipe](examples/ypipe/druid.ypipe) consider this https://github.com/iunera/ypipe/blob/main/docs/McpIntegrationBlueprints.md

Rework the readme.md and the development.md 
the default settings are starting in stdio and enabling query-only profile. 
Document the behaviour we've changed. If you are not sure about things written there asked back howto proceed with it. be careful and work over it a couple of times. 
Document which profile has which tool and which tool is using which druid api functions. 

