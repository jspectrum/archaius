package com.netflix.archaius;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigListener;
import com.netflix.archaius.api.ConfigLoader;
import com.netflix.archaius.api.ConfigLoader.Loader;
import com.netflix.archaius.api.ConfigManager;
import com.netflix.archaius.api.ConfigReader;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.Layers;
import com.netflix.archaius.api.Layer;
import com.netflix.archaius.api.config.CompositeConfig.CompositeVisitor;
import com.netflix.archaius.cascade.NoCascadeStrategy;
import com.netflix.archaius.config.AbstractConfig;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.internal.Preconditions;
import com.netflix.archaius.interpolate.ConfigStrLookup;

public final class DefaultConfigManager extends AbstractConfig implements ConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigManager.class);
    
    private static final String DEFAULT_CONFIG_NAME = "application";
    
    /**
     * Builder used to construct a DefaultLayeredConfig
     */
    public static class Builder {
        private DefaultConfigManager configManager = new DefaultConfigManager();
        
        private CascadeStrategy cascadeStrategy = NoCascadeStrategy.INSTANCE;
        
        private DefaultConfigLoader.Builder configLoaderBuilder = DefaultConfigLoader.builder();

        private String configName = DEFAULT_CONFIG_NAME;
        
        private List<Element<Consumer<ConfigManager>>> actions = new ArrayList<>();
        
        private Builder(DefaultConfigManager configManager) {
            this.configManager = configManager;
        }
        
        /**
         * Specify the cascade strategy used to determine the override permutations for a resource
         * loaded into the configuration.
         * 
         * @param strategy
         * @return Chainable builder
         */
        public Builder withCascadeStrategy(CascadeStrategy strategy) {
            Preconditions.checkState(configManager != null, "Builder already built");
            this.cascadeStrategy = strategy;
            return this;
        }
        
        /**
         * Main application configuration file to load before any other bootstrapping occurs.  The
         * default application file name is 'application'.
         * 
         * @param configName Resource name to load for the application or null to not load into the applicaiton layer
         * @return Chainable builder
         */
        public Builder withConfigName(String configName) {
            Preconditions.checkState(configManager != null, "Builder already built");
            this.configName = configName;
            return this;
        }
    
        /**
         * Additional reader to be used by the ConfigLoader.  Each readers enables a single
         * file format (such as .properties or .yaml).  Note that .properties is supported 
         * by default
         * @param reader
         * @return Chainable builder
         */
        public Builder addReader(ConfigReader reader) {
            Preconditions.checkState(configManager != null, "Builder already built");
            configLoaderBuilder.withConfigReader(reader);
            return this;
        }
    
        /**
         * Custom decoder to use instead of the build in decode
         * @param decoder
         * @return Chainable builder
         */
        public Builder withDecoder(Decoder decoder) {
            Preconditions.checkState(configManager != null, "Builder already built");
            configManager.setDecoder(decoder);
            return this;
        }
        
        /**
         * Add a resource to the specified layer.  Note that resources are loaded and added 
         * when build() is called so any loading errors will be reported then.  Also, configuration
         * is loaded in the layer and insertion order.
         * 
         * @param layer
         * @return Chainable builder
         */
        public Builder addResourceToLayer(Layer layer, String resourceName) {
            Preconditions.checkState(configManager != null, "Builder already built");
            Preconditions.checkState(resourceName != null, "Resource name must not be empty");
            LOG.info("Adding config {} to layer {}", resourceName, layer);
            actions.add(Element.create(layer, resourceName, (ConfigManager manager) -> manager.addResourceToLayer(layer, resourceName)));
            return this;
        }

        /**
         * Add a resource to the specified layer.  Note that resources are loaded and added 
         * when build() is called so any loading errors will be reported then.  Also, configuration
         * is loaded in the layer and insertion order.
         * 
         * @param layer
         * @param loader
         * @return Chainable builder
         */
        public Builder addResourceToLayer(Layer layer, String resourceName, Function<ConfigLoader.Loader, ConfigLoader.Loader> loader) {
            Preconditions.checkState(configManager != null, "Builder already built");
            Preconditions.checkState(resourceName != null , "Resource name must not be empty");
            LOG.info("Adding config {} to layer {}", resourceName, layer);
            actions.add(Element.create(layer, resourceName, (ConfigManager manager) -> manager.addResourceToLayer(layer, resourceName)));
            return this;
        }

        /**
         * Add a static configuration to the specified layer.  Configurations are added to the ConfigManager
         * is loaded in the layer and insertion order.
         * 
         * @param layer
         * @param name
         * @param props
         * @return Chainable builder
         */
        public Builder addConfigToLayer(Layer layer, String name, Properties props) {
            Preconditions.checkState(configManager != null, "Builder already built");
            LOG.info("Adding config {} to layer {}", name, layer);
            actions.add(Element.create(layer, name, (ConfigManager manager) -> manager.addConfigToLayer(layer, name, props)));
            return this;
        }
        
        /**
         * Add a static configuration to the specified layer.  Configurations are added to the ConfigManager
         * is loaded in the layer and insertion order.
         * 
         * @param layer
         * @param name
         * @param config
         * @return Chainable builder
         */
        public Builder addConfigToLayer(Layer layer, String name, Config config) {
            Preconditions.checkState(configManager != null, "Builder already built");
            LOG.info("Adding config {} to layer {}", name, layer);
            actions.add(Element.create(layer, name, (ConfigManager manager) -> manager.addConfigToLayer(layer, name, config)));
            return this;
        }
        
        /**
         * Advise the specified layer using the provider consumer.  Use this for advanced configurations such 
         * as consulting the current configuration before modifying the ConfigManager.
         * @param layer
         * @param consumer
         * @return Chainable builder
         */
        public Builder adviseLayer(Layer layer, Consumer<ConfigManager> consumer) {
            actions.add(Element.create(layer, "", consumer));
            return this;
        }
        
        @Deprecated
        public ConfigManager getRawConfigManager() {
            return configManager;
        }

        public ConfigManager build() {
            Preconditions.checkState(configManager != null, "Builder already built");
            
            configManager.configLoader = configLoaderBuilder
                    .withDefaultCascadingStrategy(cascadeStrategy)
                    .withStrLookup(ConfigStrLookup.from(configManager))
                    .build();
            
            // We do this here and not in withConfigName() so the main application configuration 
            // resource name can be overridden and we just take the final name here.
            if (configName != null) {
                addResourceToLayer(Layers.APPLICATION, configName);
            }
            
            try {
                // Add the actions in layer and insertion order.
                actions.sort(EntryComparator);
                actions.forEach(consumer -> consumer.value.accept(configManager));
                return configManager;
            } finally { 
                configManager = null;
            }
        }
    }
    
    public static Builder builder() {
        return new Builder(new DefaultConfigManager());
    }
    
    public static Builder builder(DefaultConfigManager configManager) {
        return new Builder(configManager);
    }
    
    private static class Element<T> {
        private final Layer layer;
        private final String name;
        private final int id;
        
        private final T value;
        
        private static final AtomicInteger idCounter = new AtomicInteger();
        
        static <T> Element<T> create(Layer layer, String name, T value) {
            return new Element<T>(layer, name, value);
        }
        
        private Element(Layer layer, String name, T value) {
            this.layer = layer;
            this.name = name;
            this.id = idCounter.incrementAndGet();
            this.value = value;
        }
        
        public String getName() {
            StringBuilder sb = new StringBuilder();
            sb.append(layer.getName());
            if (!name.isEmpty()) {
                sb.append(":").append(name);
            }
            return sb.toString();
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 + ((value == null) ? 0 : value.hashCode());
            result = prime * result + ((layer == null) ? 0 : layer.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Element other = (Element) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            if (layer == null) {
                if (other.layer != null)
                    return false;
            } else if (!layer.equals(other.layer))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Element [layer=" + layer + ", name=" + name + ", id=" + id + ", value=" + value + "]";
        }
    }
    
    private static final Comparator<Element<?>> EntryComparator = (Element<?> o1, Element<?> o2) -> {
        if (o1.layer != o2.layer) {
            int result = o1.layer.getOrder() - o2.layer.getOrder();
            if (result != 0) {
                return result;
            }
        }
        
        return o2.id - o1.id;
    };

    private static class State {
        private final List<Element<Config>> elements;
        
        State() {
            this.elements = Collections.emptyList();
        }
        
        State(List<Element<Config>> entries) {
            this.elements = entries;
        }
        
        State withEntries(List<Element<Config>> entries) {
            return new State(entries);
        }
    }
    
    private ConfigLoader configLoader;
    private volatile State state = new State();
    private final ConfigListener listener;
    private Set<String> loadedResources = new HashSet<>();
    
    /**
     * This method isn't really deprecated but was made public for backwards compatibility.
     * DefaultConfigManager should only be created using the builder.
     */
    @Deprecated
    public DefaultConfigManager() {
        
        listener = new ConfigListener() {
            @Override
            public void onConfigAdded(Config config) {
                notifyConfigAdded(DefaultConfigManager.this);
            }

            @Override
            public void onConfigRemoved(Config config) {
                notifyConfigRemoved(DefaultConfigManager.this);
            }

            @Override
            public void onConfigUpdated(Config config) {
                notifyConfigUpdated(DefaultConfigManager.this);
            }

            @Override
            public void onError(Throwable error, Config config) {
                notifyError(error, DefaultConfigManager.this);
            }
        };
    }
    
    @Override
    public synchronized void addResourceToLayer(Layer layer, String resourceName) {
        addResourceToLayer(layer, resourceName, loader -> loader);
    }

    @Override
    public synchronized void addResourceToLayer(Layer layer, String resourceName, Function<Loader, Loader> loader) {
        if (!loadedResources.add(resourceName)) {
            LOG.info("Resource {} already loaded", resourceName);
            return;
        }
        Preconditions.checkArgument(!resourceName.isEmpty(), "Key must have a resource name");
        addConfigToLayer(layer, resourceName, loader.apply(configLoader.newLoader()).load(resourceName));
    }

    @Override
    public synchronized void addConfigToLayer(Layer layer, String name, Properties props) {
        addConfigToLayer(layer, name, MapConfig.from(props));
    }

    @Override
    public synchronized void addConfigToLayer(Layer layer, String name, Config config) {
        Preconditions.checkArgument(config != null, "Config must not be null");
        Preconditions.checkArgument(layer != null, "Layer must not be null");
        Preconditions.checkArgument(name != null, "Name must not be null");
        
        if (state.elements.contains(config)) {
            LOG.info("Configuration with already exists");
            return;
        } 
        
        Element<Config> newItem = Element.create(layer, "", config);
        
        LOG.info("Adding configuration {}", newItem.getName());
        
        List<Element<Config>> newEntries = new ArrayList<>(state.elements);
        newEntries.add(newItem);
        newEntries.sort(EntryComparator);
        state = state.withEntries(newEntries);
        
        config.setStrInterpolator(getStrInterpolator());
        config.setDecoder(getDecoder());
        config.addListener(listener);
        notifyConfigAdded(config);
    }

    @Override
    public Object getRawProperty(String layer) {
        Preconditions.checkArgument(layer != null, "Key must not be null");
        
        State state = this.state;
        
        for (Element<Config> entry : state.elements) {
            Config config = entry.value;
            if (config.containsKey(layer)) {
                return config.getRawProperty(layer);
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(String layer) {
        Preconditions.checkArgument(layer != null, "Key must not be null");
        
        State state = this.state;
        
        for (Element<Config> entry : state.elements) {
            Config config = entry.value;
            if (config.containsKey(layer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        State state = this.state;
        
        for (Element<Config> entry : state.elements) {
            Config config = entry.value;
            if (!config.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<String> getKeys() {
        State state = this.state;
        HashSet<String> result = new HashSet<>();
        state.elements.stream().forEach(entry -> 
            entry.value
                .getKeys()
                .forEachRemaining(layer -> result.add(layer)));
        return result.iterator();
    }
    
    @Override
    public synchronized <T> T accept(Visitor<T> visitor) {
        Preconditions.checkArgument(visitor != null, "Visitor must not be null");
        
        T result = null;
        if (visitor instanceof CompositeVisitor) {
            CompositeVisitor<T> cv = (CompositeVisitor<T>)visitor;
            state.elements.forEach(entry -> {
                cv.visitChild(entry.getName(), entry.value);
            });
        }
        else {
            state.elements.forEach(entry -> {
                entry.value.accept(visitor);
            });
        }
        return result;
    }

    @Override
    public Optional<Config> getConfig(Layer layer, String resourceName) {
        Preconditions.checkArgument(layer != null, "Layer must not be null");
        Preconditions.checkArgument(resourceName != null, "Name must not be empty");
        
        List<Element<Config>> entries = state.elements;
        for (Element<Config> item : entries) {
            if (item.layer.equals(layer) && item.name.equals(resourceName)) {
                return Optional.of(item.value);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized Optional<Config> removeConfig(Layer layer, String resourceName) {
        Preconditions.checkArgument(layer != null, "Layer must not be null");
        Preconditions.checkArgument(resourceName != null, "Name must not be empty");
        
        State current = state;
        List<Element<Config>> entries = current.elements;
        for (int i = 0; i < entries.size(); i++) {
            Element<Config> item = entries.get(i);
            if (item.layer.equals(layer) && item.name.equals(resourceName)) {
                List<Element<Config>> newEntries = new ArrayList<>(entries.size());
                newEntries.addAll(entries.subList(0, i-1));
                if (i + 1 < entries.size()) {
                    newEntries.addAll(entries.subList(i+1, entries.size()));
                }
                state = current.withEntries(newEntries);
                return Optional.of(item.value);
            }
        }

        return Optional.empty();
    }
    
    @Override
    public Iterable<String> getConfigNames() {
        return state.elements.stream().map(item -> item.getName()).collect(Collectors.toList());
    }

    @Override
    @Deprecated
    public ConfigLoader getConfigLoader() {
        return configLoader;
    }
}