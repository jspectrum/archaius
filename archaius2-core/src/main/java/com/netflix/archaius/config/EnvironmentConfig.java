/**
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.archaius.config;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentConfig extends MapConfig {

    public static final EnvironmentConfig INSTANCE = new EnvironmentConfig();
    
    public EnvironmentConfig() {
        super(toStringObjectMap(System.getenv()));
    }
    
    private static Map<String, Object> toStringObjectMap(Map<String, String> src) {
        Map<String, Object> result = new HashMap<>();
        src.forEach((k,v)->result.put(k,v));
        return result;
    }
}
