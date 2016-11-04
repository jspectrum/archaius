package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.DefaultConfigManager;
import com.netflix.archaius.DefaultPropertyFactory;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigManager;
import com.netflix.archaius.api.ConfigReader;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.Layers;
import com.netflix.archaius.api.PropertyFactory;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.inject.LibrariesLayer;
import com.netflix.archaius.api.inject.RuntimeLayer;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.SystemConfig;
import com.netflix.archaius.readers.PropertiesConfigReader;
import com.netflix.governator.providers.Advises;
import com.netflix.governator.providers.ProvidesWithAdvice;

import java.util.function.UnaryOperator;

final class InternalArchaiusModule extends AbstractModule {
    
    @Override
    protected void configure() {
        ConfigurationInjectingListener listener = new ConfigurationInjectingListener();
        requestInjection(listener);
        bind(ConfigurationInjectingListener.class).toInstance(listener);
        requestStaticInjection(ConfigurationInjectingListener.class);
        bindListener(Matchers.any(), listener);
        
        Multibinder.newSetBinder(binder(), ConfigReader.class)
            .addBinding().to(PropertiesConfigReader.class).in(Scopes.SINGLETON);
    }
    
    @ProvidesWithAdvice
    @Singleton
    ConfigManager.Builder getConfigManagerBuilder() {
        return DefaultConfigManager.builder();
    }
    
    @Provides
    ConfigManager getConfigManager(ConfigManager.Builder builder) {
        return builder.build();
    }
    
    @Provides
    @Singleton
    Config getConfiguration(ConfigManager config) {
        return config.getConfig();
    }
    
    @Advises(order = Layers.ENV_LAYER_ORDER)
    @Singleton
    UnaryOperator<ConfigManager.Builder> adviseEnvLayer() {
        return builder -> builder.addLayer(Layers.ENV_LAYER, EnvironmentConfig.INSTANCE);
    }

    @Advises(order = Layers.SYS_LAYER_ORDER)
    @Singleton
    UnaryOperator<ConfigManager.Builder> adviseSysLayer() {
        return builder -> builder.addLayer(Layers.SYS_LAYER, SystemConfig.INSTANCE);
    }

    @Provides
    @Singleton
    @RuntimeLayer
    SettableConfig getSettableConfig(ConfigManager config) {
        return (SettableConfig)config.getConfigLayer(Layers.OVERRIDE_LAYER);
    }
    
    @Provides
    @Singleton
    @LibrariesLayer
    CompositeConfig getLibrariesLayer(ConfigManager config) {
        return (CompositeConfig) config.getConfigLayer(Layers.LIBRARIES_LAYER);
    }
    
    @Provides
    @Singleton
    Decoder getDecoder(ConfigManager config) {
        return config.getDecoder();
    }

    @Provides
    @Singleton
    PropertyFactory getPropertyFactory(Config config) {
        return DefaultPropertyFactory.from(config);
    }

    @Provides
    @Singleton
    ConfigProxyFactory getProxyFactory(Config config, Decoder decoder, PropertyFactory factory) {
        return new ConfigProxyFactory(config, decoder, factory);
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
