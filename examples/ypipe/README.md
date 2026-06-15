# Ypipe Integration Blueprint for Apache Druid

This folder contains the integration blueprint and configuration details for using this **Druid MCP Server** within **[Ypipe](https://ypipe.com)**.

[Ypipe](https://ypipe.com) is an AI-powered desktop client that lets you interact with your Apache Druid cluster in natural language, running models locally and securely.

## What is `druid.ypipe`?

The [druid.ypipe](druid.ypipe) file is a predefined **MCP Integration Blueprint** (`McpIntegrationBlueprint`). It specifies how Ypipe should configure, run, and communicate with the Druid MCP Server. It details:
- **Environment variables** (e.g., `DRUID_ROUTER_URL`, `DRUID_AUTH_USERNAME`, `DRUID_AUTH_PASSWORD`) needed for connection.
- **Service specifications** to auto-build or download the server and map port options.
- **Client schemas** to facilitate smooth communication.

## Screenshots

Below is the visual flow of the Druid MCP Integration inside Ypipe:

### 1. Integration Workspace Flow
This screenshot shows the workflow layout and integration nodes within the Ypipe dashboard.
![Ypipe Integration Workspace Flow](ypipe-screenshot-1.png)

### 2. Druid Node Details
This screenshot shows the configuration panel for the Apache Druid integration node in Ypipe, showing the host URL and authentication parameters.
![Ypipe Druid Node Details](ypipe-screenshot-2.png)

## How to Use

1. Ensure you have **[Ypipe](https://ypipe.com)** installed.
2. Import or reference the [druid.ypipe](druid.ypipe) blueprint in your Ypipe configuration directory.
3. Configure your Druid connection details (such as your router URL and basic credentials) through the visual node setup in the Ypipe interface.
4. Interact with your Druid cluster using natural language chat, tools, resources, and custom prompts!
