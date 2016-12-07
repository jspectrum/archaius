package com.netflix.archaius.sources.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.function.Function;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.sources.ImmutablePropertySource;

/**
 * Load a PropertySource from a URL
 * 
 * TODO: Support @next
 */
public class PropertiesToPropertySource implements Function<URL, PropertySource> {
    @Override
    public PropertySource apply(URL t) {
        Properties props = new Properties();
        try (InputStream is = t.openStream()) {
            props.load(is);
            return ImmutablePropertySource.builder()
                    .named(t.toExternalForm())
                    .putAll(props)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + t.toExternalForm());
        }
    }
}
