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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DruidMcpServerApplicationTests {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Test
    void printAllTools() throws Exception {
        Object toolSpecsObj = applicationContext.getBean("toolSpecs");
        java.util.List<?> toolSpecs = (java.util.List<?>) toolSpecsObj;
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Object spec : toolSpecs) {
            java.lang.reflect.Method toolMethod = spec.getClass().getMethod("tool");
            Object tool = toolMethod.invoke(spec);
            String name = (String) tool.getClass().getMethod("name").invoke(tool);
            names.add(name);
        }
        java.util.Collections.sort(names);
        java.nio.file.Files.write(java.nio.file.Paths.get("all_tools.txt"), names);
        System.out.println("[DEBUG_LOG] Wrote " + names.size() + " tool names to all_tools.txt");
    }

}
