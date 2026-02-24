# Gemini CLI Instructions

## Running with JBang

You can run the Druid MCP Server directly using [JBang](https://www.jbang.dev/) without having to manually build or download the jar.

### Single-line Command

To run the server using the catalog alias:

```bash
jbang druid-server@iunera/druid-mcp-server
```

*(Note: Replace `<your-github-username>` with the GitHub organization or username where this repository is hosted, e.g., `iunera`)*

### Environment Variables

Ensure you have the necessary environment variables set for your Druid cluster:

```bash
export DRUID_URL=http://localhost:8888
export DRUID_USER=admin
export DRUID_PASSWORD=Password123
jbang druid-server@iunera/druid-mcp-server
```
