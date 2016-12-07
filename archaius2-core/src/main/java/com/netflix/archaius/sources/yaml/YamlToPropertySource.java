package com.netflix.archaius.sources.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.yaml.snakeyaml.Yaml;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.sources.ImmutablePropertySource;

/**
 * Function to load a YAML file for a given URL into an ImmutablePropertySource.
 */
public class YamlToPropertySource implements Function<URL, PropertySource> {
    @Override
    public PropertySource apply(URL t) {
        Yaml yaml = new Yaml();
        
        try (InputStream is = t.openStream()) {
            Map<String, Object> values = (Map<String, Object>) yaml.load(is);

            ImmutablePropertySource.Builder builder = ImmutablePropertySource.builder()
                    .named(t.toExternalForm());
            
            traverse("", "", values, (key, value) -> builder.put(key, value));
                    
            return builder.build();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + t.toExternalForm());
        }
    }
    
    void traverse(String propertyName, String key, Object obj, BiConsumer<String, Object> consumer) {
        String newName = propertyName.isEmpty() ? key : propertyName + "." + key;
        if (obj instanceof Map) {
            ((Map<String, Object>)obj).forEach((k, v) -> traverse(newName, k, v, consumer));
        } else if (obj instanceof List) {
            int index = 0;
            for (Object element : (List)obj) {
                traverse(newName, Integer.toString(index), element, consumer);
                index++;
            }
        } else {
            consumer.accept(newName, obj);
        }
    }
}
