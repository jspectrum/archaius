package com.netflix.archaius.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;
import com.google.inject.spi.ProvisionListener;
import com.netflix.archaius.ConfigMapper;
import com.netflix.archaius.api.ArchaiusConfig;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.IoCContainer;
import com.netflix.archaius.api.annotations.Configuration;
import com.netflix.archaius.api.annotations.ConfigurationSource;
import com.netflix.archaius.api.exceptions.ConfigException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ConfigurationInjectingListener implements ProvisionListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationInjectingListener.class);
    
    @Inject
    private Config            config;
    
    @Inject
    private Injector          injector;
    
    @Inject
    private ArchaiusConfig    archaius;
    
    @Inject
    public static void init(ConfigurationInjectingListener listener) {
        LOG.info("Initializing ConfigurationInjectingListener");
    }
    
    private ConfigMapper mapper = new ConfigMapper();
    
    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        Class<?> clazz = provision.getBinding().getKey().getTypeLiteral().getRawType();
        
        //
        // Configuration Loading
        //
        final ConfigurationSource source = clazz.getDeclaredAnnotation(ConfigurationSource.class);
        if (source != null) {
            if (injector == null) {
                LOG.warn("Can't inject configuration into {} until ConfigurationInjectingListener has been initialized", clazz.getName());
                return;
            }
            
            for (String resourceName : source.value()) {
                LOG.debug("Trying to loading configuration resource {}", resourceName);
                
                try {
                    archaius.loadLibraryConfig(resourceName, loader -> {
                        if (source.cascading() != ConfigurationSource.NullCascadeStrategy.class) {
                            loader.withCascadeStrategy(injector.getInstance(source.cascading()));
                        }
                        return loader;
                    });
                } catch (ConfigException e) {
                    throw new ProvisionException("Unable to load configuration for " + resourceName, e);
                }
            }
        }
        
        //
        // Configuration binding
        //
        Configuration configAnnot = clazz.getAnnotation(Configuration.class);
        if (configAnnot != null) {
            if (injector == null) {
                LOG.warn("Can't inject configuration into {} until ConfigurationInjectingListener has been initialized", clazz.getName());
                return;
            }
            
            try {
                mapper.mapConfig(provision.provision(), config, new IoCContainer() {
                    @Override
                    public <T> T getInstance(String name, Class<T> type) {
                        return injector.getInstance(Key.get(type, Names.named(name)));
                    }
                });
            }
            catch (Exception e) {
                throw new ProvisionException("Unable to bind configuration to " + clazz, e);
            }
        }        
    }
}