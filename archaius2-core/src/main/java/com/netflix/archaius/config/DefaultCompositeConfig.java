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
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigListener;
import com.netflix.archaius.api.ConfigNode;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.exceptions.ConfigException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.function.Supplier;

/**
 * Config that is a composite of multiple configuration and as such doesn't track 
 * properties of its own.  The composite does not merge the configurations but instead
 * treats them as overrides so that a property existing in a configuration supersedes
 * the same property in configuration that was added later.  It is however possible
 * to set a flag that reverses the override order.
 * 
 * TODO: Optional cache of queried properties
 * TODO: Resolve method to collapse all the child configurations into a single config
 * TODO: Combine children and lookup into a single LinkedHashMap
 */
public class DefaultCompositeConfig extends AbstractConfig implements CompositeConfig {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCompositeConfig.class);
    
    /**
     * The builder provides a fluent style API to create a CompositeConfig
     */
    public static class Builder {
        LinkedHashMap<String, Config> configs = new LinkedHashMap<>();
        
        public Builder withConfig(String name, Config config) {
            configs.put(name, config);
            return this;
        }
        
        public CompositeConfig build() throws ConfigException {
            CompositeConfig config = new DefaultCompositeConfig();
            for (Entry<String, Config> entry : configs.entrySet()) {
                config.addConfig(entry.getKey(), entry.getValue());
            }
            return config;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static CompositeConfig create() throws ConfigException {
        return DefaultCompositeConfig.builder().build();
    }
    
    public static CompositeConfig from(LinkedHashMap<String, Config> load) throws ConfigException {
        Builder builder = builder();
        for (Entry<String, Config> config : load.entrySet()) {
            builder.withConfig(config.getKey(), config.getValue());
        }
        return builder.build();
    }
    
    /**
     * All child configurations in priority order.
     */
    private final CopyOnWriteArrayList<Config> children = new CopyOnWriteArrayList<Config>();
    
    /**
     * Lookup of child name to Config instance.
     */
    private final Map<String, Config> lookup = new LinkedHashMap<String, Config>();
    
    private final ConfigListener listener;
    
    private final boolean reversed;
    
    private ConfigNode root = this;
    
    /**
     * Cache of all child keys with pointers to the ConfigNode for each key.  The values set is
     * recomputed with each update
     */
    private AtomicStampedReference<SortedMap<String, ConfigNode>> values = 
            new AtomicStampedReference<>(new TreeMap<String, ConfigNode>(), 1);
    
    public DefaultCompositeConfig() {
        this(false);
    }
    
    public DefaultCompositeConfig(boolean reversed) {
        this.reversed = reversed;
        this.listener = new ConfigListener() {
            @Override
            public void onConfigAdded(Config config) {
                notifyConfigAdded(DefaultCompositeConfig.this);
                rebuildValueMap();
            }

            @Override
            public void onConfigRemoved(Config config) {
                notifyConfigRemoved(DefaultCompositeConfig.this);
                rebuildValueMap();
            }

            @Override
            public void onConfigUpdated(Config config) {
                notifyConfigUpdated(DefaultCompositeConfig.this);
                rebuildValueMap();
            }

            @Override
            public void onError(Throwable error, Config config) {
                notifyError(error, DefaultCompositeConfig.this);
            }
        };
    }

    @Override
    public synchronized boolean addConfig(String name, Config child) throws ConfigException {
        Preconditions.checkArgument(name != null, "Child name may not be null");
        Preconditions.checkArgument(child != null, "Child may not be null");
        
        if (lookup.containsKey(name)) {
            LOG.info("Configuration with name'{}' already exists", name);
            return false;
        }

        LOG.trace("Adding config {} to {}", name, hashCode());
        
        lookup.put(name, child);
        if (reversed) {
            children.add(0, child);
        } else {
            children.add(child);
        }
        postConfigAdded(child);
        
        rebuildValueMap();
        
        return true;
    }

    @Override
    public synchronized void addConfigs(LinkedHashMap<String, Config> configs) throws ConfigException {
        for (Entry<String, Config> entry : configs.entrySet()) {
            addConfig(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void replaceConfigs(LinkedHashMap<String, Config> configs) throws ConfigException {
        for (Entry<String, Config> entry : configs.entrySet()) {
            replaceConfig(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public synchronized Collection<String> getConfigNames() {
        return new ArrayList<String>(this.lookup.keySet());
    }
    
    protected void postConfigAdded(Config child) {
        child.setDecoder(getDecoder());
        notifyConfigAdded(child);
        child.addListener(listener);
    }

    @Override
    public synchronized void replaceConfig(String name, Config child) throws ConfigException {
        removeConfig(name);
        addConfig(name, child);
    }

    @Override
    public synchronized Config removeConfig(String name) {
        Config child = this.lookup.remove(name);
        if (child != null) {
            this.children.remove(child);
            child.removeListener(listener);
            this.notifyConfigRemoved(child);
            return child;
        }
        return null;
    }    

    @Override
    public Config getConfig(String name) {
        return lookup.get(name);
    }


    @Override
    public Object getRawProperty(String key) {
        return values.getReference().get(key).value();
    }

    @Override
    public <T> List<T> getList(String key, Class<T> type) {
        // TODO: Use values
        for (Config child : children) {
            if (child.containsKey(key)) {
                return child.getList(key, type);
            }
        }
        return notFound(key);
    }

    @Override
    public List getList(String key) {
        // TODO: Use values
        for (Config child : children) {
            if (child.containsKey(key)) {
                return child.getList(key);
            }
        }
        return notFound(key);
    }

    @Override
    public boolean containsKey(String key) {
        return values.getReference().containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return values.getReference().isEmpty();
    }

    /**
     * Return a set of all unique keys tracked by any child of this composite.
     * This can be an expensive operations as it requires iterating through all of
     * the children.
     */
    @Override
    public Iterable<String> keys() {
        return values.getReference().keySet();
    }
    
    @Override
    public Iterator<String> getKeys(String prefix) {
        return child(prefix).keys().iterator();
    }

    @Override
    public synchronized <T> T accept(Visitor<T> visitor) {
        T result = null;
        if (visitor instanceof CompositeVisitor) {
            synchronized (this) {
                CompositeVisitor<T> cv = (CompositeVisitor<T>)visitor;
                for (Entry<String, Config> entry : lookup.entrySet()) {
                    result = cv.visitChild(entry.getKey(), entry.getValue());
                }
            }
        }
        else {
            for (Config child : children) {
                result = child.accept(visitor);
            }
        }
        return result;
    }

    private synchronized void rebuildValueMap() {
        final SortedMap<String, ConfigNode> values = new TreeMap<>();
        accept(new CompositeVisitor<Void>() {
            @Override
            public Void visitKey(Config config, String key) {
                if (!values.containsKey(key)) {
                    values.put(key, config);
                }
                return null;
            }

            @Override
            public Void visitChild(String name, Config child) {
                child.accept(this);
                return null;
            }
        });
        this.values.set(values, this.values.getStamp() + 1);
    }
    
    @Override
    public ConfigNode child(String prefix) { 
        Preconditions.checkArgument(prefix != null && !prefix.isEmpty());
        Preconditions.checkArgument(!prefix.endsWith("."));

        final Supplier<SortedMap<String, ConfigNode>> supplier = new Supplier<SortedMap<String, ConfigNode>>() {
            private AtomicStampedReference<SortedMap<String, ConfigNode>> cache 
                = new AtomicStampedReference<>(new TreeMap<>(), 0);
            
            @Override
            public SortedMap<String, ConfigNode> get() {
                do {
                    int[] currentStamp = new int[1];
                    SortedMap<String, ConfigNode> current = cache.get(currentStamp);
                    
                    int[] mainStamp = new int[1];
                    SortedMap<String, ConfigNode> root = values.get(mainStamp);
                    
                    if (currentStamp[0] == mainStamp[0]) {
                        return cache.getReference();
                    } else {
                        SortedMap<String, ConfigNode> newSub = root.subMap( prefix + ".", prefix + "./uffff");
                        if (cache.compareAndSet(current, newSub, currentStamp[0], mainStamp[0])) {
                            return newSub;
                        }
                    }
                } while (true);
            }
        };
        
        return new ConfigNode() {
            @Override
            public ConfigNode child(String prefix2) {
                return DefaultCompositeConfig.this.child(prefix + "." + prefix2);
            }

            @Override
            public ConfigNode root() {
                return root;
            }

            @Override
            public Object value() {
                return supplier.get().get(prefix);
            }

            @Override
            public boolean containsKey(String key) {
                return supplier.get().containsKey(prefix + "." + key);
            }

            @Override
            public boolean isEmpty() {
                return supplier.get().isEmpty();
            }

            @Override
            public Iterable<String> keys() {
                return supplier.get().keySet();
            }
        };
    }

    @Override
    public ConfigNode root() {
        return root;
    }

    @Override
    public Object value() {
        // Composite config has no implicit value
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        for (Config child : children) {
            sb.append(child.toString()).append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
