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

import com.netflix.archaius.ScalarNode;
import com.netflix.archaius.SortedMapChildNode;
import com.netflix.archaius.api.DataNode;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Config backed by an immutable map.
 */
public class MapConfig extends AbstractConfig {
    /**
     * The builder only provides convenience for fluent style adding of properties
     * 
     * {@code
     * <pre>
     * MapConfig.builder()
     *      .put("foo", "bar")
     *      .put("baz", 123)
     *      .build()
     * </pre>
     * }
     */
    public static class Builder {
        MapConfig config = new MapConfig();
        
        public <T> Builder put(String key, T value) {
            config.props.put(key, ScalarNode.from(value, config));
            return this;
        }
        
        public MapConfig build() {
            try {
                return config;
            } finally {
                config = null;
            }
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static MapConfig from(Properties props) {
        return new MapConfig(props);
    }
    
    public static MapConfig from(Map<String, Object> props) {
        return new MapConfig(props);
    }
    
    private final SortedMap<String, DataNode> props = new TreeMap<String, DataNode>();
    
    private MapConfig() {
    }
    
    /**
     * Construct a MapConfig as a copy of the provided Map
     * @param name
     * @param props
     */
    public MapConfig(Map<String, Object> props) {
        props.forEach((k, v) -> this.props.put(k, ScalarNode.from(v, this)));
    }

    /**
     * Construct a MapConfig as a copy of the provided properties
     * @param name
     * @param props
     */
    public MapConfig(Properties props) {
        props.forEach((k, v) -> this.props.put(k.toString(), ScalarNode.from(v, this)));
    }
    
    @Override
    public Object getRawProperty(String key) {
        return props.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return props.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return props.isEmpty();
    }

    @Override
    public Iterator<String> getKeys() {
        return props.keySet().iterator();
    }

    @Override
    public DataNode child(String name) {
        return new SortedMapChildNode(this, props, name);
    }
}
