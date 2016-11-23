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
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.inject.RuntimeLayer;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.SystemConfig;
import com.netflix.archaius.readers.PropertiesConfigReader;
import com.netflix.governator.providers.AdvisableAnnotatedMethodScanner;
import com.netflix.governator.providers.ProvidesWithAdvice;

final class InternalArchaiusModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(AdvisableAnnotatedMethodScanner.asModule());
        
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
    DefaultConfigManager.Builder getConfigManagerBuilder(@RuntimeLayer SettableConfig settableConfig) {
        return DefaultConfigManager.builder()
            .addConfigToLayer(Layers.ENVIRONMENT, "", EnvironmentConfig.INSTANCE)
            .addConfigToLayer(Layers.SYSTEM,      "", SystemConfig.INSTANCE)
            .addConfigToLayer(Layers.OVERRIDE,    "", settableConfig)
            ;
    }
    
    @Provides
    @Singleton
    ConfigManager getConfigManager(DefaultConfigManager.Builder builder) {
        return builder.build();
    }
    
    @Provides
    @Singleton
    Config getConfiguration(ConfigManager config) {
        return config;
    }
    
    @Provides
    @Singleton
    @RuntimeLayer
    SettableConfig getSettableConfig() {
        return new DefaultSettableConfig();
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
        return obj != null && getClass().equals(obj.getClass());
    }
}
