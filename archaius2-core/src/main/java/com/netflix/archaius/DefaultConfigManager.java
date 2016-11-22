package com.netflix.archaius;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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
        private DefaultConfigManager rootConfig = new DefaultConfigManager();
        
        private CascadeStrategy cascadeStrategy = NoCascadeStrategy.INSTANCE;
        
        private DefaultConfigLoader.Builder configLoaderBuilder = DefaultConfigLoader.builder();

        private String configName = DEFAULT_CONFIG_NAME;
        
        private List<Element<Consumer<ConfigManager>>> actions = new ArrayList<>();
        
        private Builder() {
        }
        
        /**
         * Specify the cascade strategy used to determine the override permutations for a resource
         * loaded into the configuration.
         * 
         * @param strategy
         * @return Chainable builder
         */
        public Builder withCascadeStrategy(CascadeStrategy strategy) {
            Preconditions.checkState(rootConfig != null, "Builder already built");
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
            Preconditions.checkState(rootConfig != null, "Builder already built");
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
            Preconditions.checkState(rootConfig != null, "Builder already built");
            configLoaderBuilder.withConfigReader(reader);
            return this;
        }
    
        /**
         * Custom decoder to use instead of the build in decode
         * @param decoder
         * @return Chainable builder
         */
        public Builder withDecoder(Decoder decoder) {
            Preconditions.checkState(rootConfig != null, "Builder already built");
            rootConfig.setDecoder(decoder);
            return this;
        }
        
        /**
         * Add a resource to the specified layer.  Note that resources are loaded and added 
         * when build() is called so any loading errors will be reported then.  Also, configuration
         * is loaded in the layer and insertion order.
         * 
         * @param key
         * @return Chainable builder
         */
        public Builder addResourceToLayer(Key key) {
            Preconditions.checkState(rootConfig != null, "Builder already built");
            Preconditions.checkState(!key.getResourceName().isEmpty(), "Resource name must not be empty");
            actions.add(Element.create(key, (ConfigManager manager) -> manager.addResourceToLayer(key)));
            return this;
        }

        /**
         * Add a resource to the specified layer.  Note that resources are loaded and added 
         * when build() is called so any loading errors will be reported then.  Also, configuration
         * is loaded in the layer and insertion order.
         * 
         * @param key
         * @param loader
         * @return Chainable builder
         */
        public Builder addResourceToLayer(Key key, Function<ConfigLoader.Loader, ConfigLoader.Loader> loader) {
            Preconditions.checkState(rootConfig != null, "Builder already built");
            Preconditions.checkState(!key.getResourceName().isEmpty(), "Resource name must not be empty");
            actions.add(Element.create(key, (ConfigManager manager) -> manager.addResourceToLayer(key)));
            return this;
        }

        /**
         * Add a static configuration to the specified layer.  Configurations are added to the ConfigManager
         * is loaded in the layer and insertion order.
         * 
         * @param key
         * @param name
         * @param props
         * @return Chainable builder
         */
        public Builder addConfigToLayer(Key key, Properties props) {
            Preconditions.checkState(rootConfig != null, "Builder already built");
           actions.add(Element.create(key, (ConfigManager manager) -> manager.addConfigToLayer(key, props)));
            return this;
        }
        
        /**
         * Add a static configuration to the specified layer.  Configurations are added to the ConfigManager
         * is loaded in the layer and insertion order.
         * 
         * @param key
         * @param name
         * @param config
         * @return Chainable builder
         */
        public Builder addConfigToLayer(Key key, Config config) {
            Preconditions.checkState(rootConfig != null, "Builder already built");
            actions.add(Element.create(key, (ConfigManager manager) -> manager.addConfigToLayer(key, config)));
            return this;
        }
        
        /**
         * Advise the specified layer using the provider consumer.  Use this for advanced configurations such 
         * as consulting the current configuration before modifying the ConfigManager.
         * @param key
         * @param consumer
         * @return Chainable builder
         */
        public Builder adviseLayer(Key key, Consumer<ConfigManager> consumer) {
            actions.add(Element.create(key, consumer));
            return this;
        }

        public ConfigManager build() {
            Preconditions.checkState(rootConfig != null, "Builder already built");
            
            rootConfig.configLoader = configLoaderBuilder
                    .withDefaultCascadingStrategy(cascadeStrategy)
                    .withStrLookup(ConfigStrLookup.from(rootConfig))
                    .build();
            
            // We do this here and not in withConfigName() so the main application configuration 
            // resource name can be overridden and we just take the final name here.
            if (configName != null) {
                addResourceToLayer(Layers.APPLICATION.resource(configName));
            }
            
            try {
                // Add the actions in layer and insertion order.
                actions.sort(EntryComparator);
                actions.forEach(consumer -> consumer.value.accept(rootConfig));
                return rootConfig;
            } finally { 
                rootConfig = null;
            }
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private static class Element<T> {
        private final Key key;
        private final int id;
        
        private final T value;
        
        private static final AtomicInteger idCounter = new AtomicInteger();
        
        static <T> Element<T> create(Key key, T value) {
            return new Element<T>(key, value);
        }
        
        private Element(Key key, T value) {
            this.key = key;
            this.id = idCounter.incrementAndGet();
            this.value = value;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 + ((value == null) ? 0 : value.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
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
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }
    }
    
    private static final Comparator<Element<?>> EntryComparator = (Element<?> o1, Element<?> o2) -> {
        if (o1.key != o2.key) {
            int result = o2.key.getOrder() - o1.key.getOrder();
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
    
    private DefaultConfigManager() {
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
    public void addResourceToLayer(Key key) {
        Preconditions.checkArgument(!key.getResourceName().isEmpty(), "Key must have a resource name");
        addConfigToLayer(key, configLoader.newLoader().load(key.getResourceName()));
    }

    @Override
    public void addResourceToLayer(Key key, Function<Loader, Loader> loader) {
        Preconditions.checkArgument(!key.getResourceName().isEmpty(), "Key must have a resource name");
        addConfigToLayer(key, loader.apply(configLoader.newLoader()).load(key.getResourceName()));
    }

    @Override
    public void addConfigToLayer(Key key, Properties props) {
        Preconditions.checkArgument(!key.getResourceName().isEmpty(), "Key must have a resource name");
        addConfigToLayer(key, MapConfig.from(props));
    }

    @Override
    public synchronized void addConfigToLayer(Key key, Config config) {
        Preconditions.checkArgument(config != null, "Config must not be null");
        Preconditions.checkArgument(key != null, "Layer must not be null");
        
        if (state.elements.contains(config)) {
            LOG.info("Configuration with name'{}' already exists", key.getResourceName());
            return;
        } 
        
        Element<Config> newItem = Element.create(key, config);
        
        LOG.info("Adding configuration {}", newItem.key.getFullName());
        
        List<Element<Config>> newEntries = new ArrayList<>(state.elements);
        newEntries.add(new Element<Config>(key, config));
        newEntries.sort(EntryComparator);
        state = state.withEntries(newEntries);
        
        config.setStrInterpolator(getStrInterpolator());
        config.setDecoder(getDecoder());
        config.addListener(listener);
        notifyConfigAdded(config);
    }

    @Override
    public Object getRawProperty(String key) {
        Preconditions.checkArgument(key != null, "Key must not be null");
        
        State state = this.state;
        
        for (Element<Config> entry : state.elements) {
            Config config = entry.value;
            if (config.containsKey(key)) {
                return config.getRawProperty(key);
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        Preconditions.checkArgument(key != null, "Key must not be null");
        
        State state = this.state;
        
        for (Element<Config> entry : state.elements) {
            Config config = entry.value;
            if (config.containsKey(key)) {
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
                .forEachRemaining(key -> result.add(key)));
        return result.iterator();
    }
    
    @Override
    public synchronized <T> T accept(Visitor<T> visitor) {
        Preconditions.checkArgument(visitor != null, "Visitor must not be null");
        
        T result = null;
        if (visitor instanceof CompositeVisitor) {
            CompositeVisitor<T> cv = (CompositeVisitor<T>)visitor;
            state.elements.forEach(entry -> {
                cv.visitChild(entry.key.getFullName(), entry.value);
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
    public Optional<Config> getConfig(Key layer) {
        Preconditions.checkArgument(layer != null, "Layer must not be null");
        Preconditions.checkArgument(layer.getResourceName() != null, "Name must not be empty");
        
        List<Element<Config>> entries = state.elements;
        for (Element<Config> item : entries) {
            if (item.key.equals(layer) && item.key.equals(layer)) {
                return Optional.of(item.value);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized Optional<Config> removeConfig(Key key) {
        Preconditions.checkArgument(key != null, "Layer must not be null");
        Preconditions.checkArgument(!key.getResourceName().isEmpty(), "Name must not be empty");
        
        State current = state;
        List<Element<Config>> entries = current.elements;
        for (int i = 0; i < entries.size(); i++) {
            Element<Config> item = entries.get(i);
            if (item.key.equals(key) && item.key.equals(key)) {
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
        return state.elements.stream().map(item -> item.key.getFullName()).collect(Collectors.toList());
    }
}