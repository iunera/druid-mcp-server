#!/bin/bash

#
# Copyright (C) 2025 Christian Schmitt, Tim Frey
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

# Script to update version numbers across all documentation and configuration files
# Usage: ./scripts/update-version.sh <new-version>
# Example: ./scripts/update-version.sh 1.3.0

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 <new-version>"
    echo ""
    echo "This script updates version numbers in all project files:"
    echo "  - pom.xml (Maven project version)"
    echo "  - src/main/resources/application.properties (MCP server version)"
    echo "  - server.json (MCP registry version and Docker image tag)"
    echo "  - mcpservers-stdio.json (Docker image tag)"
    echo "  - README.md (JAR file references)"
    echo "  - All README files under examples/ (JAR and Docker image tag references)"
    echo "  - development.md (version references in documentation)"
    echo "  - mcp-registry.md (Docker image tag references)"
    echo ""
    echo "Examples:"
    echo "  $0 1.3.0       # Update to version 1.3.0"
    echo "  $0 2.0.0-RC1   # Update to release candidate"
}

# Function to validate version format
validate_version() {
    local version="$1"
    if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?$ ]]; then
        print_error "Invalid version format: $version"
        print_error "Expected format: X.Y.Z or X.Y.Z-SUFFIX (e.g., 1.3.0, 2.0.0-RC1)"
        return 1
    fi
    return 0
}

# Function to get current version from pom.xml
get_current_version() {
    if [ -f "pom.xml" ]; then
        # Extract the project version (skip parent version) - look for the version after artifactId
        sed -n '/<artifactId>druid-mcp-server<\/artifactId>/,/<version>/p' pom.xml | grep -o '<version>[^<]*</version>' | sed 's/<version>\([^<]*\)<\/version>/\1/'
    else
        print_error "pom.xml not found"
        return 1
    fi
}

# Function to update pom.xml
update_pom_xml() {
    local new_version="$1"
    local file="pom.xml"
    
    if [ ! -f "$file" ]; then
        print_error "$file not found"
        return 1
    fi

    # Update the project version (first occurrence)
    sed -i.tmp "0,/<version>.*<\/version>/s/<version>.*<\/version>/<version>$new_version<\/version>/" "$file"
    rm -f "${file}.tmp"
    
    print_success "Updated $file"
}

# Function to update application.properties
update_application_properties() {
    local new_version="$1"
    local file="src/main/resources/application.properties"
    
    if [ ! -f "$file" ]; then
        print_error "$file not found"
        return 1
    fi

    # Update spring.ai.mcp.server.version
    sed -i.tmp "s/spring\.ai\.mcp\.server\.version=.*/spring.ai.mcp.server.version=$new_version/" "$file"
    rm -f "${file}.tmp"
    
    print_success "Updated $file"
}

# Function to update server.json
update_server_json() {
    local new_version="$1"
    local file="server.json"
    
    if [ ! -f "$file" ]; then
        print_error "$file not found"
        return 1
    fi

    # Update version fields and Docker image tag
    sed -i.tmp "s/\"version\": \"[^\"]*\"/\"version\": \"$new_version\"/g" "$file"
    sed -i.tmp -E "s/iunera\/druid-mcp-server:[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?\"/iunera\/druid-mcp-server:$new_version\"/g" "$file"
    rm -f "${file}.tmp"
    
    print_success "Updated $file"
}

# Function to update mcpservers-stdio.json
update_mcpservers_stdio_json() {
    local new_version="$1"
    local file="mcpservers-stdio.json"
    
    if [ ! -f "$file" ]; then
        print_error "$file not found"
        return 1
    fi

    # Update Docker image tag (skip :latest)
    sed -i.tmp -E "s/iunera\/druid-mcp-server:[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?\"/iunera\/druid-mcp-server:$new_version\"/g" "$file"
    rm -f "${file}.tmp"
    
    print_success "Updated $file"
}

# Function to update development.md
update_development_md() {
    local new_version="$1"
    local file="development.md"
    
    if [ ! -f "$file" ]; then
        print_error "$file not found"
        return 1
    fi

    # Update spring.ai.mcp.server.version reference in configuration examples
    sed -i.tmp "s/spring\.ai\.mcp\.server\.version=.*/spring.ai.mcp.server.version=$new_version/" "$file"
    # Update JAR file references
    sed -i.tmp -E "s/druid-mcp-server-[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?\.jar/druid-mcp-server-$new_version.jar/g" "$file"
    rm -f "${file}.tmp"
    
    print_success "Updated $file"
}

# Function to update mcp-registry.md
update_mcp_registry_md() {
    local new_version="$1"
    local file="mcp-registry.md"
    
    if [ ! -f "$file" ]; then
        print_error "$file not found"
        return 1
    fi

    # Update Docker image tag references
    sed -i.tmp -E "s/iunera\/druid-mcp-server:[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?/iunera\/druid-mcp-server:$new_version/g" "$file"
    rm -f "${file}.tmp"
    
    print_success "Updated $file"
}

# Function to update all README files
update_all_readmes() {
    local new_version="$1"

    # Update main README.md (JAR references)
    local file="README.md"
    if [ -f "$file" ]; then
        sed -i.tmp -E "s/druid-mcp-server-[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?\.jar/druid-mcp-server-$new_version.jar/g" "$file"
        # Also update Docker image tag if present in root README (skip :latest)
        sed -i.tmp -E "s/iunera\/druid-mcp-server:[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?/iunera\/druid-mcp-server:$new_version/g" "$file"
        rm -f "${file}.tmp"
        print_success "Updated $file"
    fi

    # Update all README files under examples/ using find from repository root
    while IFS= read -r -d '' file; do
        # Update JAR references
        sed -i.tmp -E "s/druid-mcp-server-[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?\.jar/druid-mcp-server-$new_version.jar/g" "$file"
        # Update Docker image tag references (skip :latest)
        sed -i.tmp -E "s/iunera\/druid-mcp-server:[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?/iunera\/druid-mcp-server:$new_version/g" "$file"
        rm -f "${file}.tmp"
        print_success "Updated $file"
    done < <(find ./examples -type f \( -iname 'readme.md' -o -iname 'readme' \) -print0)
}

# Function to show summary of changes
show_summary() {
    local new_version="$1"
    local old_version="$2"
    
    echo ""
    print_info "=== VERSION UPDATE SUMMARY ==="
    echo "Old version: $old_version"
    echo "New version: $new_version"
    echo ""
    echo "Updated files:"
    echo "  ✓ pom.xml"
    echo "  ✓ src/main/resources/application.properties"
    echo "  ✓ server.json"
    echo "  ✓ mcpservers-stdio.json"
    echo "  ✓ README.md"
    echo "  ✓ all README files under examples/"
    echo "  ✓ development.md"
    echo "  ✓ mcp-registry.md"
    echo ""
    print_warning "Next steps:"
    echo "  1. Review the changes: git diff"
    echo "  2. Test the build: mvn clean package"
    echo "  3. Commit the changes: git add . && git commit -m 'Update version to $new_version'"
    echo "  4. Push to trigger release: git push origin main"
}


# Main script logic
main() {
    # Check if we're in the project root
    if [ ! -f "pom.xml" ]; then
        print_error "This script must be run from the project root directory"
        exit 1
    fi
    
    # Check arguments
    if [ $# -ne 1 ]; then
        show_usage
        exit 1
    fi
    
    local new_version="$1"
    
    # Validate version format
    if ! validate_version "$new_version"; then
        exit 1
    fi
    
    # Get current version
    local current_version
    current_version=$(get_current_version)
    if [ $? -ne 0 ]; then
        exit 1
    fi
    
    print_info "Current version: $current_version"
    print_info "New version: $new_version"
    
    # Confirm the update
    echo ""
    read -p "Do you want to proceed with the version update? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Version update cancelled."
        exit 0
    fi
    
    print_info "Starting version update..."
    echo ""
    
    # Update all files
    update_pom_xml "$new_version"
    update_application_properties "$new_version"
    update_server_json "$new_version"
    update_mcpservers_stdio_json "$new_version"
    update_development_md "$new_version"
    update_mcp_registry_md "$new_version"
    update_all_readmes "$new_version"
    
    # Show summary
    show_summary "$new_version" "$current_version"
    
    print_success "Version update completed successfully!"
}

# Run main function
main "$@"