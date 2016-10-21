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
import com.netflix.archaius.ScalarNode;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigNode;
import com.netflix.archaius.api.config.SettableConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

public class DefaultSettableConfig extends AbstractConfig implements SettableConfig {
    private volatile SortedMap<String, Object> values = new TreeMap<>();
    
    @Override
    public synchronized <T> void setProperty(String key, T value) {
        values.put(key, ScalarNode.from(value, this));
        notifyConfigUpdated(this);
    }
    
    @Override
    public synchronized void clearProperty(String key) {
        values.remove(key);
        notifyConfigUpdated(this);
    }

    @Override
    public synchronized boolean containsKey(String key) {
        return values.containsKey(key);
    }

    @Override
    public synchronized boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public synchronized Object getRawProperty(String key) {
        return values.get(key);
    }
    
    @Override
    public synchronized Iterable<String> keys() {
        return new ArrayList<>(values.keySet());
    }

    @Override
    public synchronized void setProperties(Properties properties) {
        if (null != properties) {
            properties.forEach((k, v) -> 
                values.put(k.toString(), ScalarNode.from(v, DefaultSettableConfig.this)));
        }
    }

    @Override
    public synchronized void setProperties(Config config) {
        if (null != config) {
            config.accept(new Visitor<Void>() {
                @Override
                public Void visitKey(Config config, String key) {
                    values.put(key, ScalarNode.from(config.getRawProperty(key), DefaultSettableConfig.this));
                    return null;
                }
            });
        }
    }

    @Override
    public synchronized <T> T accept(Visitor<T> visitor) {
        T result = null;
        Iterator<String> iter = values.keySet().iterator();
        while (iter.hasNext()) {
            result = visitor.visitKey(this, iter.next());
        }
        return result;
    }

    @Override
    public synchronized ConfigNode child(String prefix) {
        Preconditions.checkArgument(!prefix.endsWith("."));
        Supplier<SortedMap<String, Object>> supplier = () -> values.subMap(prefix + ".", prefix + "./uffff");
        return new ConfigNode() {
            private volatile SortedMap<String, Object> childProps = supplier.get();
            
            @Override
            public ConfigNode child(String prefix2) {
                return DefaultSettableConfig.this.child(prefix + "." + prefix2);
            }

            @Override
            public ConfigNode root() {
                return DefaultSettableConfig.this;
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
    public Iterator<String> getKeys(String prefix) {
        return child(prefix).keys().iterator();
    }

    @Override
    public Object value() {
        return null;
    }
}
