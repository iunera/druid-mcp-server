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

package com.iunera.druidmcpserver;

import com.iunera.druidmcpserver.datamanagement.compaction.CompactionConfigToolProvider;
import com.iunera.druidmcpserver.datamanagement.compaction.CompactionPromptProvider;
import com.iunera.druidmcpserver.datamanagement.datasource.DatasourceResources;
import com.iunera.druidmcpserver.datamanagement.datasource.DatasourceToolProvider;
import com.iunera.druidmcpserver.datamanagement.lookup.LookupResources;
import com.iunera.druidmcpserver.datamanagement.lookup.LookupToolProvider;
import com.iunera.druidmcpserver.datamanagement.query.DataAnalysisPromptProvider;
import com.iunera.druidmcpserver.datamanagement.query.QueryToolProvider;
import com.iunera.druidmcpserver.datamanagement.retention.RetentionPromptProvider;
import com.iunera.druidmcpserver.datamanagement.retention.RetentionRulesToolProvider;
import com.iunera.druidmcpserver.datamanagement.segments.SegmentResources;
import com.iunera.druidmcpserver.datamanagement.segments.SegmentToolProvider;
import com.iunera.druidmcpserver.ingestion.IngestionManagementPromptProvider;
import com.iunera.druidmcpserver.ingestion.spec.IngestionSpecToolProvider;
import com.iunera.druidmcpserver.ingestion.supervisors.SupervisorsToolProvider;
import com.iunera.druidmcpserver.ingestion.tasks.TasksToolProvider;
import com.iunera.druidmcpserver.monitoring.health.basic.HealthToolProvider;
import com.iunera.druidmcpserver.monitoring.health.diagnostics.DruidDoctorToolProvider;
import com.iunera.druidmcpserver.monitoring.health.functionality.FunctionalityHealthToolProvider;
import com.iunera.druidmcpserver.monitoring.health.prompts.ClusterManagementPromptProvider;
import com.iunera.druidmcpserver.operations.OperationalPromptProvider;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class DruidMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DruidMcpServerApplication.class, args);
    }

//    @Bean
//    public ToolCallbackProvider druidTools(
//            QueryToolProvider queryToolProvider,
//            CompactionConfigToolProvider compactionConfigToolProvider,
//            DatasourceToolProvider datasourceToolProvider,
//            LookupToolProvider lookupToolProvider,
//            RetentionRulesToolProvider retentionRulesToolProvider,
//            IngestionSpecToolProvider ingestionSpecToolProvider,
//            SupervisorsToolProvider supervisorsToolProvider,
//            TasksToolProvider tasksToolProvider,
//            SegmentToolProvider segmentToolProvider,
//            HealthToolProvider healthToolProvider,
//            DruidDoctorToolProvider druidDoctorToolProvider,
//            FunctionalityHealthToolProvider functionalityHealthToolProvider,
//            @org.springframework.beans.factory.annotation.Value("${readonly.enabled:false}") boolean readonlyEnabled
//    ) {
//        java.util.List<Object> toolObjects = new java.util.ArrayList<>();
//        if (readonlyEnabled) {
//            // Register only read-only tools
//            toolObjects.add(queryToolProvider);
//            toolObjects.add(healthToolProvider);
//            toolObjects.add(druidDoctorToolProvider);
//            toolObjects.add(functionalityHealthToolProvider);
//        } else {
//            // Register all tools
//            toolObjects.add(queryToolProvider);
//            toolObjects.add(compactionConfigToolProvider);
//            toolObjects.add(datasourceToolProvider);
//            toolObjects.add(retentionRulesToolProvider);
//            toolObjects.add(ingestionSpecToolProvider);
//            toolObjects.add(supervisorsToolProvider);
//            toolObjects.add(tasksToolProvider);
//            toolObjects.add(lookupToolProvider);
//            toolObjects.add(segmentToolProvider);
//            toolObjects.add(healthToolProvider);
//            toolObjects.add(druidDoctorToolProvider);
//            toolObjects.add(functionalityHealthToolProvider);
//        }
//        return MethodToolCallbackProvider.builder().toolObjects(toolObjects.toArray()).build();
//    }
//
//    @Bean
//    public List<SyncResourceSpecification> resourceSpecs(DatasourceResources datasourceResourceProvider,
//                                                         SegmentResources segmentResourceProvider,
//                                                         LookupResources lookupResourceProvider) {
//        return SpringAiMcpAnnotationProvider.createSyncResourceSpecifications(
//                List.of(datasourceResourceProvider, segmentResourceProvider, lookupResourceProvider));
//    }
//
//    @Bean
//    public List<SyncPromptSpecification> promptSpecs(DataAnalysisPromptProvider dataAnalysisPromptProvider,
//                                                     ClusterManagementPromptProvider clusterManagementPromptProvider,
//                                                     IngestionManagementPromptProvider ingestionManagementPromptProvider,
//                                                     RetentionPromptProvider retentionPromptProvider,
//                                                     CompactionPromptProvider compactionPromptProvider,
//                                                     OperationalPromptProvider operationalPromptProvider) {
//        return SpringAiMcpAnnotationProvider.createSyncPromptSpecifications(
//                List.of(dataAnalysisPromptProvider, clusterManagementPromptProvider,
//                        ingestionManagementPromptProvider, retentionPromptProvider,
//                        compactionPromptProvider, operationalPromptProvider));
//    }
}
