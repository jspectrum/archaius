package com.netflix.archaius;

import com.google.common.base.Preconditions;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigLoader;
import com.netflix.archaius.api.ConfigLoader.Loader;
import com.netflix.archaius.api.ConfigManager;
import com.netflix.archaius.api.ConfigReader;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.cascade.NoCascadeStrategy;
import com.netflix.archaius.config.DefaultCompositeConfig;
import com.netflix.archaius.interpolate.ConfigStrLookup;
import com.netflix.archaius.visitor.PrintStreamVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class DefaultConfigManager implements ConfigManager {
    
    static class BuilderImpl implements ConfigManager.Builder {
        private DefaultConfigManager configManager = new DefaultConfigManager();
        
        private CascadeStrategy cascadeStrategy = NoCascadeStrategy.INSTANCE;
        
        private final DefaultConfigLoader.Builder configLoaderBuilder;
    
        private List<Runnable> actions = new ArrayList<>();
        
        public BuilderImpl() {
            configLoaderBuilder = DefaultConfigLoader.builder();
        }
        
        @Override
        public ConfigManager.Builder setCascadeStrategy(CascadeStrategy strategy) {
            Preconditions.checkNotNull(configManager, "Builder already built");
            this.cascadeStrategy = strategy;
            return this;
        }
    
        @Override
        public Builder addReader(ConfigReader reader) {
            Preconditions.checkNotNull(configManager, "Builder already built");
            configLoaderBuilder.withConfigReader(reader);
            return this;
        }
    
        @Override
        public Builder addCompositeLayer(String layer) {
            Preconditions.checkNotNull(configManager, "Builder already built");
            configManager.config.addConfig(layer, new DefaultCompositeConfig());
            return this;
        }
    
        @Override
        public Builder addLayer(String layer, Config config) {
            Preconditions.checkNotNull(configManager, "Builder already built");
            configManager.config.addConfig(layer, config);
            return this;
        }
    
        @Override
        public Builder addResourceToLayer(String layer, String resourceName) {
            Preconditions.checkNotNull(configManager, "Builder already built");
            this.actions.add(() -> {
                // TODO: Throw if not composite
                getOrAddLayer(layer).addConfig(resourceName, configManager.configLoader.newLoader().load(resourceName));
            });
            return this;
        }
    
        @Override
        public Builder addConfigToLayer(String layer, String name, Config config) {
            Preconditions.checkNotNull(configManager, "Builder already built");
            this.actions.add(() -> {
                // TODO: Throw if not composite
                getOrAddLayer(layer).addConfig(name, config);
            });
            return this;
        }
    
        @Override
        public Builder addConfigToLayer(String layer, String name, Function<Config, Config> func) {
            Preconditions.checkNotNull(configManager, "Builder already built");
            this.actions.add(() -> {
                // TODO: Throw if not composite
                getOrAddLayer(layer).addConfig(name, func.apply(configManager.config));
            });
            return this;
        }
        
        private CompositeConfig getOrAddLayer(String layer) {
            Preconditions.checkNotNull(configManager, "Builder already built");
            CompositeConfig config = (CompositeConfig) configManager.config.getConfig(layer);
            if (config == null) {
                config = new DefaultCompositeConfig();
                configManager.config.addConfig(layer, config);
            }
            return config;
        }
        
        public ConfigManager build() {
            Preconditions.checkNotNull(configManager, "Builder already built");
            
            configManager.configLoader = configLoaderBuilder
                    .withDefaultCascadingStrategy(cascadeStrategy)
                    .withStrLookup(ConfigStrLookup.from(configManager.config))
                    .build();
            
            actions.forEach(action -> action.run());
    
            configManager.config.accept(new PrintStreamVisitor());
            
            try {
                return configManager;
            } finally { 
                configManager = null;
            }
        }
    }
    
    public static ConfigManager.Builder builder() {
        return new BuilderImpl();
    }
    
    private final CompositeConfig config = new DefaultCompositeConfig();
    private ConfigLoader configLoader;

    DefaultConfigManager() {
        config.setDecoder(new DefaultDecoder());
    }
    
    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Decoder getDecoder() {
        return config.getDecoder();
    }

    @Override
    public Config getConfigLayer(String layer) {
        return config.getConfig(layer);
    }

    @Override
    public Config addResourceToLayer(String layer, String resourceName) {
        return addConfigToLayer(layer, resourceName, configLoader.newLoader().load(resourceName));
    }

    @Override
    public Config addConfigToLayer(String layer, String name, Config childConfig) {
        ((CompositeConfig)config
            .getConfig(layer))
            .addConfig(name, childConfig);
        return childConfig;
    }

    @Override
    public Config addConfigToLayer(String layer, String resourceName, Function<Loader, Loader> loader) {
        return addConfigToLayer(layer, resourceName, loader.apply(configLoader.newLoader()).load(resourceName));
    }

}