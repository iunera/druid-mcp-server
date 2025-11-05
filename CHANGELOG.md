# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project adheres to Semantic Versioning.

## [1.5.2] - 2025-11-05

### Changed
- Dependency refreshes.
- Spring AI: upgraded `spring.ai` from `1.1.0-M3` to `1.1.0-M4`
- Updated MCP SDK to `0.15.0`: unified request context API, improved autoconfiguration for MCP tool initialization, and fixed tool callback provider injection issues.
- Dockerfile improvements and healthcheck fix:
  - Reworked to a multi-stage build with a dedicated `deps` stage to cache Maven dependencies and reuse the local repository, reducing image build time.
  - Upgraded base build/runtime images to Eclipse Temurin 25.
  - Switched the container healthcheck to use `nc -z localhost 8080` (netcat) instead of `curl`. 

Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.5.1...v1.5.2

## [1.5.1] - 2025-10-15

### Fix
- Versioning and MCP Registry publish

Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.5.0...v1.5.1


## [1.5.0] - 2025-10-10

### Breaking Change
`Streamable HTTP` is now to be configured by setting `SPRING_PROFILES_ACTIVE=http`

### Added
- Added failsafe methods to `SecurityRepository` to prevent modification or deletion of critical "admin" and "druid_system" roles and users, and to disallow password changes for the "druid_system" user, ensuring their immutability.
- Introduced a new feedback tool to gather community input on `druid-mcp-server` usage and prioritize future feature development.
- Implemented anonymous metrics for product enhancement. Opt-out is available via `druid.mcp.metrics.enabled` property.
- Introduced two Spring profiles with dedicated configs: `stdio` (default) via `application-stdio.yaml` and `http` (SSE/HTTP) via `application-http.yaml`.
- Updated to Spring AI 1.1.0-M3.

### Changed
- Set `stdio` as the default Spring profile (non-web, STDIO transport).

## [1.4.0] - 2025-10-02

### Added
- Introduced basic-security components to support Apache Druid basic authentication and authorization modification via MCP
- Added support for toggling server Druid Basic Security Tools on/off with `DRUID_EXTENSION_DRUID_BASIC_SECURITY_ENABLED`
- Expanded test coverage for basic-security flows and readonly interactions.
- Prepared CI/CD release workflows and documentation updates for the 1.4.0 release.

### Notes
- This release adds foundational basic-security tooling and read-only controls; deployments should verify environment variables (DRUID_ROUTER_URL, DRUID_AUTH_USERNAME/DRUID_AUTH_PASSWORD) and enable security features as required.
- DRUID_COORDINATOR_URL needs to be configured to use the Druid Basic Security Tools

Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.3.0...v1.4.0

## [1.3.0] - 2025-09-26

### Breaking Change
Configurable Environment Variable `DRUID_MCP_READONLY` is now `DRUID_MCP_READONLY_ENABLED`

### Added
- Introduced OAuth2 security for authenticating clients.
- Migrated configuration from `application.properties` to `application.yaml`.
- MCP Inspection Cli examples 

### Changed
- Refactored Druid configuration classes.

Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.2.2...v1.3.0

## [1.2.2] - 2025-09-24

Release version 1.2.2

MCP protocol version 2025-06-18 support

What's Changed
- Add druid-mcp-server to Github MCP registry
- Update spring-ai 1.1.0-M2

Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.2.1...v1.2.2

## [1.2.1] - 2025-09-23

Release version 1.2.1

Changes
- Label Dockerimage for Github MCP registry
- Spring Boot 3.5.6
- 

Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.2.0...v1.2.1

## [1.2.0] - 2025-09-19

Release version 1.2.0

Changes
- Introduce Readonly Mode

Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.1.0...v1.2.0

## [1.1.0] - 2025-09-18

Release version 1.1.0

Changes
- Partially support 2025-06-18 incl. Streamable HTTP
- Update spring-ai 1.1.0-M1


Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.0.1...v1.1.0

## [1.0.1] - 2025-07-08

Release version 1.0.1

Changes
- Automated release from pom.xml version 1.0.1

Full Changelog: https://github.com/iunera/druid-mcp-server/compare/v1.0.0...v1.0.1

## [1.0.0] - 2025-07-08

Release version 1.0.0

Changes
- Initial public release

---

[1.4.0]: https://github.com/iunera/druid-mcp-server/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/iunera/druid-mcp-server/compare/v1.2.2...v1.3.0
[1.2.2]: https://github.com/iunera/druid-mcp-server/releases/tag/v1.2.2
[1.2.1]: https://github.com/iunera/druid-mcp-server/releases/tag/v1.2.1
[1.2.0]: https://github.com/iunera/druid-mcp-server/releases/tag/v1.2.0
[1.1.0]: https://github.com/iunera/druid-mcp-server/releases/tag/v1.1.0
[1.0.1]: https://github.com/iunera/druid-mcp-server/releases/tag/v1.0.1
[1.0.0]: https://github.com/iunera/druid-mcp-server/releases/tag/v1.0.0
