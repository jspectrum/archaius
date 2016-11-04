package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigManager;
import com.netflix.archaius.api.Layers;
import com.netflix.archaius.api.inject.DefaultLayer;
import com.netflix.archaius.api.inject.RemoteLayer;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.SystemConfig;
import com.netflix.governator.providers.Advises;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import javax.inject.Named;
import javax.inject.Provider;

public final class LegacyInternalArchaiusModule extends AbstractModule {
    static final String CONFIG_NAME_KEY         = "archaius.config.name";

    private final static AtomicInteger uniqueNameCounter = new AtomicInteger();

    private static String getUniqueName(String prefix) {
        return prefix +"-" + uniqueNameCounter.incrementAndGet();
    }
    
    @Override
    protected void configure() {
    }

    @Singleton
    private static class ConfigParameters {
        @Inject(optional=true)
        @Named(CONFIG_NAME_KEY)
        private String configName;
        
        @Inject(optional=true)
        CascadeStrategy       cascadingStrategy;
        
        @Inject(optional=true)
        @RemoteLayer 
        private Provider<Config> remoteLayerProvider;
        
        @Inject(optional=true)
        @DefaultLayer 
        private Set<Config> defaultConfigs;
        
        @Inject(optional=true)
        @ApplicationOverride
        private Config applicationOverride;

        @Inject(optional =true)
        @ApplicationOverrideResources
        private Set<String> overrideResources;
        
        Set<Config> getDefaultConfigs() {
            return defaultConfigs != null ? defaultConfigs : Collections.emptySet();
        }

        Optional<Provider<Config>> getRemoteLayer() {
            return Optional.ofNullable(remoteLayerProvider);
        }

        Set<String> getOverrideResources() {
            return overrideResources != null ? overrideResources : Collections.emptySet();
        }
        
        Optional<String> getConfigName() {
            return Optional.ofNullable(configName);
        }
        
        Optional<CascadeStrategy> getCascadeStrategy() {
            return Optional.ofNullable(cascadingStrategy);
        }
        
        Optional<Config> getApplicationOverride() {
            return Optional.ofNullable(applicationOverride);
        }
    }

    @Advises(order = 0)
    @Singleton
    UnaryOperator<ConfigManager.Builder> adviseCore(ConfigParameters params) throws Exception {
        return builder -> {
            // Order matters here!
            params.getCascadeStrategy()
                .ifPresent(strategy -> builder.setCascadeStrategy(strategy));
            
            return builder;
        };
    }

    @Advises(order = Layers.OVERRIDE_LAYER_ORDER - 1)
    @Singleton
    UnaryOperator<ConfigManager.Builder> adviseOptionalOverrideLayer(ConfigParameters params) throws Exception {
        return builder -> {
            // Order matters here!
            builder.addLayer(Layers.OVERRIDE_LAYER, new DefaultSettableConfig());
            
            params.getOverrideResources()
                .forEach(resource -> builder.addResourceToLayer(Layers.OVERRIDE_LAYER, resource));
            
            return builder;
        };
    }

    @Advises(order = Layers.APPLICATION_LAYER_ORDER - 1)
    @Singleton
    UnaryOperator<ConfigManager.Builder> adviseOptionalApplicationLayer(ConfigParameters params) throws Exception {
        return builder -> {
            builder.addCompositeLayer(Layers.APPLICATION_LAYER);
            
            params.getApplicationOverride()
                .ifPresent(config -> builder.addConfigToLayer(Layers.APPLICATION_LAYER, getUniqueName("override"), config));
            
            params.getConfigName()
                .ifPresent(name -> builder.addResourceToLayer(Layers.APPLICATION_LAYER, name));
            
            return builder;
        };
    }

    @Advises(order = Layers.LIBRARIES_LAYER_ORDER - 1)
    @Singleton
    UnaryOperator<ConfigManager.Builder> adviseOptionalLibrariesLayer(ConfigParameters params) throws Exception {
        return builder -> {
            // Order matters here!
            builder.addCompositeLayer(Layers.LIBRARIES_LAYER);
            
            return builder;
        };
    }

    @Advises(order = Layers.DEFAULT_LAYER_ORDER - 1)
    @Singleton
    UnaryOperator<ConfigManager.Builder> adviseDefaultLayer(ConfigParameters params) throws Exception {
        return builder -> {
            // Order matters here!
            builder.addCompositeLayer(Layers.DEFAULT_LAYER);
            
            params.getDefaultConfigs()
                .forEach(config -> builder.addConfigToLayer(Layers.DEFAULT_LAYER, getUniqueName("default"), config));
            
            return builder;
        };
    }
    
    @Advises(order = Layers.REMOTE_LAYER_ORDER - 1)
    @Singleton
    UnaryOperator<ConfigManager.Builder> adviseRemoteLayer(ConfigParameters params) throws Exception {
        return builder -> {
            // TODO: Need to make sure there's no circular dependency here where the remote layer needs the current
            // config.  It's better to use a function
            params.getRemoteLayer().map(Provider::get)
                .ifPresent(config -> builder.addConfigToLayer(Layers.REMOTE_LAYER, getUniqueName("remote"), config));
            
            return builder;
        };
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

}
