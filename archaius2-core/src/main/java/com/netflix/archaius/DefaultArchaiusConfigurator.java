package com.netflix.archaius;

import com.netflix.archaius.api.ArchaiusConfig;
import com.netflix.archaius.api.ArchaiusConfig.Configurator;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigLoader;
import com.netflix.archaius.api.ConfigLoader.Loader;
import com.netflix.archaius.api.ConfigReader;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.MatchingDecoder;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.config.DefaultCompositeConfig;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.SystemConfig;
import com.netflix.archaius.interpolate.ConfigStrLookup;
import com.netflix.archaius.visitor.PrintStreamVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public final class DefaultArchaiusConfigurator implements ArchaiusConfig.Configurator {
    static final String CONFIG_NAME_KEY         = "archaius.config.name";
    
    private static final String DEFAULT_CONFIG_NAME     = "application";
    
    private static final String RUNTIME_LAYER_NAME      = "RUNTIME";
    private static final String REMOTE_LAYER_NAME       = "REMOTE";
    private static final String SYSTEM_LAYER_NAME       = "SYSTEM";
    private static final String ENVIRONMENT_LAYER_NAME  = "ENVIRONMENT";
    private static final String APPLICATION_LAYER_NAME  = "APPLICATION";
    private static final String LIBRARIES_LAYER_NAME    = "LIBRARIES";
    private static final String DEFAULT_LAYER_NAME      = "DEFAULT";
    
    private final CompositeConfig config = new DefaultCompositeConfig();
    
    private final CompositeConfig applicationLayer = new DefaultCompositeConfig();
    private final CompositeConfig remoteLayer = new DefaultCompositeConfig();
    private final CompositeConfig librariesLayer = new DefaultCompositeConfig();
    private final SettableConfig runtimeLayer = new DefaultSettableConfig();
    private final CompositeConfig defaultLayer = new DefaultCompositeConfig();

    private CascadeStrategy cascadeStrategy;
    
    private final DefaultConfigLoader.Builder configLoaderBuilder;

    private ConfigLoader configLoader;
    
    private final List<MatchingDecoder> decoders = new ArrayList<>();
    
    private final List<Runnable> applicationLayerActions = new ArrayList<>();
    private final List<Runnable> remoteLayerActions = new ArrayList<>();
    private final List<Runnable> defaultLayerActions = new ArrayList<>();

    private String applicationName = DEFAULT_CONFIG_NAME;
    
    public static ArchaiusConfig create(Set<Consumer<Configurator>> consumers) {
        DefaultArchaiusConfigurator configurator = new DefaultArchaiusConfigurator();
        consumers.forEach(consumer -> consumer.accept(configurator));
        return configurator.create();
    }

    private DefaultArchaiusConfigurator() {
        config.addConfig(RUNTIME_LAYER_NAME,      runtimeLayer);
        config.addConfig(REMOTE_LAYER_NAME,       remoteLayer);
        config.addConfig(SYSTEM_LAYER_NAME,       SystemConfig.INSTANCE);
        config.addConfig(ENVIRONMENT_LAYER_NAME,  EnvironmentConfig.INSTANCE);
        config.addConfig(APPLICATION_LAYER_NAME,  applicationLayer);
        config.addConfig(LIBRARIES_LAYER_NAME,    librariesLayer);
        config.addConfig(DEFAULT_LAYER_NAME,      defaultLayer);
        
        configLoaderBuilder = DefaultConfigLoader.builder();
    }
    
    @Override
    public Configurator setCascadeStrategy(CascadeStrategy strategy) {
        this.cascadeStrategy = strategy;
        return this;
    }

    @Override
    public Configurator addReader(ConfigReader reader) {
        configLoaderBuilder.withConfigReader(reader);
        return this;
    }

    @Override
    public Configurator addDecoder(MatchingDecoder decoder) {
        this.decoders.add(decoder);
        return this;
    }

    @Override
    public Configurator addOverrideConfig(Config config) {
        runtimeLayer.setProperties(config);
        return this;
    }
    
    @Override
    public Configurator addApplicationOverrideResource(String name) {
        applicationLayerActions.add(() -> applicationLayer.addConfig(name, configLoader.newLoader().load(name)));
        return this;
    }
    
    @Override
    public Configurator setApplicationName(String name) {
        this.applicationName  = name;
        return this;
    }

    @Override
    public Configurator addDefaultConfig(String name, Function<Config, Config> func) {
        defaultLayerActions.add(() -> defaultLayer.addConfig(name, config));
        return this;
    }

   @Override
    public Configurator addApplicationOverrideConfig(String name, Function<Config, Config> func) {
        this.applicationLayerActions.add(() -> applicationLayer.addConfig(name, func.apply(config)));
        return this;
    }

    @Override
    public Configurator addRemoteConfig(String name, Function<Config, Config> func) {
        this.remoteLayerActions.add(() -> remoteLayer.addConfig(name, func.apply(config)));
        return this;
    }

    ArchaiusConfig create() {
        config.setDecoder(new DefaultDecoder(decoders));
        
        configLoader = configLoaderBuilder
                .withDefaultCascadingStrategy(this.cascadeStrategy)
                .withStrLookup(ConfigStrLookup.from(config))
                .build();
        
        this.addApplicationOverrideResource(applicationName);
        
        // Order here is important since we keep passing in the same config reference as it's being
        // built.  Default first, then application, then remote.
        defaultLayerActions.forEach(action -> action.run());
        applicationLayerActions.forEach(action -> action.run());
        remoteLayerActions.forEach(action -> action.run());

        config.accept(new PrintStreamVisitor());
        
        return new ArchaiusConfig() {
            @Override
            public Config getConfig() {
                return config;
            }

            @Override
            public SettableConfig getOverrideConfig() {
                return runtimeLayer;
            }

            @Override
            public void loadLibraryConfig(String name) {
                librariesLayer.addConfig(name, configLoader.newLoader().load(name));
            }
            
            @Override
            public void loadLibraryConfig(String name, Function<Loader, Loader> loader) {
                librariesLayer.addConfig(name, loader.apply(configLoader.newLoader()).load(name));
            }

            @Override
            public CompositeConfig getLibrariesConfig() {
                return librariesLayer;
            }

            @Override
            public Decoder getDecoder() {
                return config.getDecoder();
            }
        };
    }
}