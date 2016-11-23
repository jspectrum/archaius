package com.netflix.archaius.guice;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import javax.inject.Named;
import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.archaius.DefaultConfigManager;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigManager;
import com.netflix.archaius.api.Layers;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.inject.DefaultLayer;
import com.netflix.archaius.api.inject.LibrariesLayer;
import com.netflix.archaius.api.inject.RemoteLayer;
import com.netflix.governator.providers.Advises;

public final class LegacyInternalArchaiusModule extends AbstractModule {
    public static final String CONFIG_NAME_KEY         = "archaius.config.name";
    public static final int LEGACY_ADVICE_ORDER = 10;
    
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

    @Advises(order = LEGACY_ADVICE_ORDER)
    @Singleton
    UnaryOperator<DefaultConfigManager.Builder> adviseOptionalOverrideLayer(ConfigParameters params) throws Exception {
        return builder -> {
            params.getCascadeStrategy()
                .ifPresent(strategy -> builder.withCascadeStrategy(strategy));
            
            params.getConfigName().ifPresent(name -> builder.withConfigName(name));
            
            params.getOverrideResources()
                .forEach(resourceName -> builder.addResourceToLayer(Layers.OVERRIDE, resourceName));
            
            params.getApplicationOverride()
                .ifPresent(config -> builder.addConfigToLayer(Layers.APPLICATION_OVERRIDE, "", config));
            
            params.getDefaultConfigs()
                .forEach(config -> builder.addConfigToLayer(Layers.DEFAULT, "", config));
            
            params.getRemoteLayer().map(Provider::get)
                .ifPresent(config -> builder.addConfigToLayer(Layers.REMOTE_OVERRIDE, "", config));
            
            return builder;
        };
    }

    @Provides
    @Singleton
    @LibrariesLayer
    CompositeConfig getLegacyLibrariesLayerConfig(ConfigManager configManager) {
        return new LegacyLibraryLayerCompositeConfig(configManager);
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
