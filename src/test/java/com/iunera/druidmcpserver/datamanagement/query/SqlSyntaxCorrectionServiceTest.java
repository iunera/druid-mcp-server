/*
 * Copyright (C) 2025 Christian Schmitt, Tim Frey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iunera.druidmcpserver.datamanagement.query;

import com.iunera.druidmcpserver.config.DruidProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SqlSyntaxCorrectionServiceTest {

    private DruidMetadataLoader metadataLoader;
    private DruidProperties druidProperties;
    private SqlSyntaxCorrectionService service;

    @BeforeEach
    void setUp() {
        metadataLoader = Mockito.mock(DruidMetadataLoader.class);
        druidProperties = Mockito.mock(DruidProperties.class);
        
        // Mock properties to return enabled = true by default
        DruidProperties.Mcp mcp = Mockito.mock(DruidProperties.Mcp.class);
        DruidProperties.Mcp.SqlSyntaxCorrection config = Mockito.mock(DruidProperties.Mcp.SqlSyntaxCorrection.class);
        when(config.isEnabled()).thenReturn(true);
        when(mcp.getSqlSyntaxCorrection()).thenReturn(config);
        when(druidProperties.getMcp()).thenReturn(mcp);
        
        service = new SqlSyntaxCorrectionService(metadataLoader, druidProperties);
    }

    @Test
    void testDisabledBehavior() {
        // Arrange
        DruidProperties.Mcp.SqlSyntaxCorrection config = druidProperties.getMcp().getSqlSyntaxCorrection();
        when(config.isEnabled()).thenReturn(false);
        String sql = "SELECT * FROM potsdam-v8;";

        // Act
        String result = service.correctQuerySyntax(sql);

        // Assert
        assertEquals(sql, result);
    }

    @Test
    void testSemicolonRemoval() {
        // Arrange
        when(metadataLoader.fetchSchemaMetadata()).thenReturn(Collections.emptyMap());

        // Act & Assert
        assertEquals("SELECT 1", service.correctQuerySyntax("SELECT 1;"));
        assertEquals("SELECT 1", service.correctQuerySyntax("SELECT 1;   "));
        assertEquals("SELECT 1", service.correctQuerySyntax("SELECT 1"));
    }

    @Test
    void testTableCasingAndQuoting() {
        // Arrange
        Map<String, Set<String>> mockMetadata = new HashMap<>();
        mockMetadata.put("potsdam-v8", Set.of("city", "population"));
        when(metadataLoader.fetchSchemaMetadata()).thenReturn(mockMetadata);

        // Act & Assert
        // Hyphenated table should be quoted and case-corrected
        assertEquals("SELECT * FROM \"potsdam-v8\"", service.correctQuerySyntax("SELECT * FROM potsdam-V8"));
        assertEquals("SELECT * FROM \"potsdam-v8\"", service.correctQuerySyntax("SELECT * FROM \"potsdam-v8\""));
        
        // Check surrounding characters (preceded by spaces, parenthesis, operators)
        assertEquals("SELECT * FROM \"potsdam-v8\" WHERE 1=1", service.correctQuerySyntax("SELECT * FROM potsdam-v8 WHERE 1=1"));
        assertEquals("SELECT * FROM(\"potsdam-v8\")", service.correctQuerySyntax("SELECT * FROM(potsdam-v8)"));
    }

    @Test
    void testColumnQuoting() {
        // Arrange
        Map<String, Set<String>> mockMetadata = new HashMap<>();
        mockMetadata.put("potsdam-v8", Set.of("city", "population", "year"));
        when(metadataLoader.fetchSchemaMetadata()).thenReturn(mockMetadata);

        // Act & Assert
        // Unquoted columns of referenced tables should be double-quoted
        assertEquals("SELECT \"city\", \"population\" FROM \"potsdam-v8\"", 
                service.correctQuerySyntax("SELECT city, population FROM potsdam-v8"));

        // Already quoted columns should not be double-quoted again
        assertEquals("SELECT \"city\", \"population\" FROM \"potsdam-v8\"", 
                service.correctQuerySyntax("SELECT \"city\", population FROM potsdam-v8"));
    }

    @Test
    void testAvoidQuotingKeywordsAndLiterals() {
        // Arrange
        Map<String, Set<String>> mockMetadata = new HashMap<>();
        mockMetadata.put("potsdam-v8", Set.of("city", "limit", "select"));
        when(metadataLoader.fetchSchemaMetadata()).thenReturn(mockMetadata);

        // Act & Assert
        // SQL keywords should not be quoted (like limit or select)
        assertEquals("SELECT \"city\" FROM \"potsdam-v8\" LIMIT 10", 
                service.correctQuerySyntax("SELECT city FROM potsdam-v8 LIMIT 10"));

        // Strings inside single quotes (literals) should be untouched
        assertEquals("SELECT \"city\" FROM \"potsdam-v8\" WHERE \"city\" = 'potsdam-v8'", 
                service.correctQuerySyntax("SELECT city FROM potsdam-v8 WHERE city = 'potsdam-v8'"));

        // Comments should be untouched
        assertEquals("SELECT \"city\" FROM \"potsdam-v8\" -- select city here", 
                service.correctQuerySyntax("SELECT city FROM potsdam-v8 -- select city here"));
    }
}
