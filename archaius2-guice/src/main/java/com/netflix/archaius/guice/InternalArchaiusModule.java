package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.MultibindingsScanner;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.DefaultArchaiusConfigurator;
import com.netflix.archaius.DefaultPropertyFactory;
import com.netflix.archaius.api.ArchaiusConfig;
import com.netflix.archaius.api.ArchaiusConfig.Configurator;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigReader;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.PropertyFactory;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.inject.DefaultLayer;
import com.netflix.archaius.api.inject.LibrariesLayer;
import com.netflix.archaius.api.inject.RemoteLayer;
import com.netflix.archaius.api.inject.RuntimeLayer;
import com.netflix.archaius.readers.PropertiesConfigReader;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.inject.Named;
import javax.inject.Provider;

final class InternalArchaiusModule extends AbstractModule {
    static final String CONFIG_NAME_KEY         = "archaius.config.name";
    
    private static AtomicInteger uniqueNameCounter = new AtomicInteger();

    private static String getUniqueName(String prefix) {
        return prefix +"-" + uniqueNameCounter.incrementAndGet();
    }
    
    @Override
    protected void configure() {
        install(MultibindingsScanner.asModule());
        
        ConfigurationInjectingListener listener = new ConfigurationInjectingListener();
        requestInjection(listener);
        bind(ConfigurationInjectingListener.class).toInstance(listener);
        requestStaticInjection(ConfigurationInjectingListener.class);
        bindListener(Matchers.any(), listener);
        
        Multibinder.newSetBinder(binder(), ConfigReader.class)
            .addBinding().to(PropertiesConfigReader.class).in(Scopes.SINGLETON);
    }
    
    @Provides
    @Singleton
    ArchaiusConfig getArchaiusConfig(Set<Consumer<Configurator>> consumers) {
        return DefaultArchaiusConfigurator.create(consumers);
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

    @ProvidesIntoSet
    @Singleton
    Consumer<Configurator> getCoreConfigurator(ConfigParameters params) throws Exception {
        return configurator -> {
            params.getDefaultConfigs()
                .forEach(config -> configurator.addDefaultConfig(getUniqueName("default"), rawConfig -> config));
            
            params.getOverrideResources()
                .forEach(resource -> configurator.addApplicationOverrideResource(resource));
            
            params.getApplicationOverride()
                .ifPresent(config -> configurator.addApplicationOverrideConfig(getUniqueName("override"), rawConfig -> config));
            
            params.getConfigName()
                .ifPresent(name -> configurator.setApplicationName(name));
            
            params.getRemoteLayer().map(Provider::get)
                .ifPresent(config -> configurator.addRemoteConfig(getUniqueName("remote"), rawConfig -> config));
            
            params.getCascadeStrategy()
                .ifPresent(strategy -> configurator.setCascadeStrategy(strategy));
        };
    }
        
    @Provides
    @Singleton
    @RuntimeLayer
    SettableConfig getSettableConfig(ArchaiusConfig config) {
        return config.getOverrideConfig();
    }
    
    @Provides
    @Singleton
    Config getConfiguration(ArchaiusConfig config) {
        return config.getConfig();
    }
    
    @Provides
    @Singleton
    @LibrariesLayer
    CompositeConfig getLibrariesLayer(ArchaiusConfig config) {
        return config.getLibrariesConfig();
    }
    
    @Provides
    @Singleton
    Decoder getDecoder(ArchaiusConfig config) {
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
