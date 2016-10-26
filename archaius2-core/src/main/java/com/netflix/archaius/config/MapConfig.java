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

import com.google.common.base.Preconditions;
import com.netflix.archaius.api.ConfigNode;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

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
            Preconditions.checkState(config != null, "Builder cannot be mutate after build() is called");
            config.values.put(key, value);
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
    
    private final SortedMap<String, Object> values = new TreeMap<String, Object>();
    
    private MapConfig() {
    }
    
    /**
     * Construct a MapConfig as a copy of the provided Map
     * @param name
     * @param props
     */
    public MapConfig(Map<String, Object> props) {
        this.values.putAll(props);
    }

    /**
     * Construct a MapConfig as a copy of the provided properties
     * @param name
     * @param props
     */
    public MapConfig(Properties props) {
        props.forEach((key, value) -> this.values.put(key.toString(), value));
    }
    
    @Override
    public Object getRawProperty(String key) {
        return values.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public Iterable<String> keys() {
        return values.keySet();
    }

    @Override
    public ConfigNode child(String prefix) {
        Preconditions.checkArgument(prefix != null && !prefix.isEmpty());
        Preconditions.checkArgument(!prefix.endsWith("."));
        Supplier<SortedMap<String, Object>> supplier = () -> values.subMap(prefix + ".", prefix + "./uffff");
        return new ConfigNode() {
            private volatile SortedMap<String, Object> childProps = supplier.get();
            
            @Override
            public ConfigNode child(String name) {
                return MapConfig.this.child(prefix + "." + name);
            }

            @Override
            public ConfigNode root() {
                return MapConfig.this;
            }

            @Override
            public Object value() {
                return childProps.get(prefix);
            }

            @Override
            public boolean containsKey(String key) {
                return childProps.containsKey(prefix + "." + key);
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public Iterable<String> keys() {
                return childProps.keySet();
            }
        };
    }
    
    @Override
    public Object value() {
        // There's no such thing as a root value
        return null;
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
        return child(prefix).keys().iterator();
    }
}
