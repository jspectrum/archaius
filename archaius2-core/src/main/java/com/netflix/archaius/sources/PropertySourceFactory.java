package com.netflix.archaius.sources;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.api.Bundle;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.archaius.sources.properties.PropertiesToPropertySource;
import com.netflix.archaius.sources.yaml.YamlToPropertySource;

/**
 * Factory for returning a PropertySource for a resource {@link Bundle}.  The returned PropertySource
 * will contain properties from all found cascade resource name variants interpolated using the property
 * PropertySource, for all supported formats.  Note that if multiple files exist, all will be loaded.
 */
public class PropertySourceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(PropertySourceFactory.class);
    
    private Map<String, Function<URL, PropertySource>> factories = new HashMap<>();
    private List<Function<String, List<URL>>> urlResolvers = new ArrayList<>();
    private Function<String, String> interpolator;
    
    public PropertySourceFactory(PropertySource source) {
        factories.put("properties", new PropertiesToPropertySource());
        factories.put("yml", new YamlToPropertySource(source));
        
        urlResolvers.add((name) -> {
            try {
                return Collections.list(ClassLoader.getSystemResources(name));
            } catch (Exception e) {
                return Collections.emptyList();
            }
        });
        
        StrInterpolator.Lookup lookup = key -> source.getProperty(key).map(Object::toString).orElse(null);
        this.interpolator = value -> CommonsStrInterpolator.INSTANCE.create(lookup).resolve((String)value);
    }
    
    public PropertySource create(Bundle bundle) {
        return new CompositePropertySource(
            bundle.getName(), 
            bundle.getCascadeGenerator().apply(bundle.getName()).stream()
                // All resource name variants (without extension)
                .flatMap((String name) -> factories.entrySet().stream()
                    // All supported file formats
                    .flatMap((entry) -> urlResolvers.stream()
                        // Load each file
                        .flatMap((resolver) -> {
                            String interpolated = interpolator.apply(name + "." + entry.getKey());
                            LOG.info("Loading url {}", interpolated);
                            return resolver.apply(interpolated).stream()
                                .map(url -> entry.getValue().apply(url));
                        })
                    )
                )
                .filter(s -> s != null)
                .collect(Collectors.toList()));
    }
}
