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

