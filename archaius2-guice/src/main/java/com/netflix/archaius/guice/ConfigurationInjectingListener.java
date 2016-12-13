package com.netflix.archaius.guice;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;
import com.google.inject.spi.ProvisionListener;
import com.netflix.archaius.ConfigMapper;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.IoCContainer;
import com.netflix.archaius.api.Layers;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.api.annotations.Configuration;
import com.netflix.archaius.api.annotations.ConfigurationSource;
import com.netflix.archaius.api.exceptions.ConfigException;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.archaius.sources.LayeredPropertySource;

@Singleton
public class ConfigurationInjectingListener implements ProvisionListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationInjectingListener.class);
    
    @Inject
    private Injector injector;
    
    @Inject
    private Configuration config;
    
    @Inject
    private CascadeStrategy cascadeStrategy;
    
    private LayeredPropertySource propertySource;
    
    private StrInterpolator interpolator;
    
    @Inject
    private void setLayeredPropertySource(LayeredPropertySource propertySource) {
        this.propertySource = propertySource;
        StrInterpolator.Lookup lookup = key -> delegate.getProperty(key).map(Object::toString).orElse(null);
        this.interpolator = value -> {
            if (value.getClass() == String.class) {
                return CommonsStrInterpolator.INSTANCE.create(lookup).resolve((String)value);
            }
            return value;
        };
    }    
    
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
            
            Arrays.asList(source.value()).forEach(bundleName -> {
                LOG.debug("Trying to loading configuration bundle {}", bundleName);
                    Stream.of(source.cascading() != ConfigurationSource.NullCascadeStrategy.class
                            ? injector.getInstance(source.cascading())
                            : cascadeStrategy)
                        .flatMap(strategy -> strategy.generate(bundleName, , lookup));
                            
                    });
            });
            for (String resourceName : source.value()) {
                
                
                try {
                    
                    propertySource.addPropertySource(Layers.LIBRARIES, resourceName, loader -> {
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
                    public <S> S getInstance(String name, Class<S> type) {
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