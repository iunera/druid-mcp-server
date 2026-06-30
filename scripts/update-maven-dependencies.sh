#!/usr/bin/env bash
#
# Copyright (C) 2026 Christian Schmitt, Tim Frey
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -euo pipefail

# Update Maven dependencies for druid-mcp-server.
#
# Usage:
#   scripts/update-maven-dependencies.sh                # perform updates
#   scripts/update-maven-dependencies.sh --dry-run      # show available updates only
#   scripts/update-maven-dependencies.sh --help

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_POM="$ROOT_DIR/pom.xml"

DRY_RUN=false
# Pattern to ignore EA, Milestone, Alpha, Beta, and RC versions
IGNORE_VERSION_REGEX="(?i).*-(ea|m|milestone|alpha|beta|rc)[-._0-9]?.*"

print_help() {
  sed -n '1,16p' "$0" | sed 's/^# \{0,1\}//'
}

while [[ ${1-} ]]; do
  case "$1" in
    --dry-run)
      DRY_RUN=true
      ;;
    --help|-h)
      print_help
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      exit 1
      ;;
  esac
  shift
done

# Check for mvn or ./mvnw
if command -v mvn >/dev/null 2>&1; then
  MVN_CMD="mvn"
elif [[ -x "$ROOT_DIR/mvnw" ]]; then
  MVN_CMD="$ROOT_DIR/mvnw"
else
  echo "Error: mvn or mvnw not found" >&2
  exit 1
fi

[[ -f "$TARGET_POM" ]] || { echo "Error: missing $TARGET_POM" >&2; exit 1; }

echo "Repository root: $ROOT_DIR"
echo "Target POM: $TARGET_POM"
echo "Maven command: $MVN_CMD"

if $DRY_RUN; then
  echo "Mode: DRY-RUN"
  $MVN_CMD -f "$TARGET_POM" -B versions:display-property-updates versions:display-dependency-updates versions:display-plugin-updates \
    -Dmaven.version.ignore="$IGNORE_VERSION_REGEX"
  echo "Dry-run complete."
  exit 0
fi

echo "Mode: UPDATE"

echo "Step 1/3: Updating properties (versions defined in <properties>)"
$MVN_CMD -f "$TARGET_POM" -B versions:update-properties -DgenerateBackupPoms=false \
  -Dmaven.version.ignore="$IGNORE_VERSION_REGEX"

echo "Step 2/3: Updating dependencies to latest releases"
$MVN_CMD -f "$TARGET_POM" -B versions:use-latest-releases -DgenerateBackupPoms=false \
  -Dmaven.version.ignore="$IGNORE_VERSION_REGEX"

echo "Step 3/3: Updating plugin versions to latest releases"
$MVN_CMD -f "$TARGET_POM" -B versions:use-latest-versions -DgenerateBackupPoms=false -DprocessPlugins=true -DprocessDependencies=false \
  -Dmaven.version.ignore="$IGNORE_VERSION_REGEX"

echo "Cleanup: Removing any pom.xml.versionsBackup files"
find "$ROOT_DIR" -name "pom.xml.versionsBackup" -delete

echo "Done. Maven dependency, property, and plugin versions updated."
