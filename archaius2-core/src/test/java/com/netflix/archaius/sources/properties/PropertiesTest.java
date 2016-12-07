package com.netflix.archaius.sources.properties;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class PropertiesTest {
    @Test
    public void loadExisting() {
        Map<String, Object> properties = Stream.of("test.properties")
            .map(ClassLoader::getSystemResource)
            .map(new PropertiesToPropertySource())
            .flatMap(source -> source.stream())
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        
        Assert.assertEquals(4,  properties.size());
    }
    
    @Test
    public void ignoreMissingFile() {
        Map<String, Object> properties = Stream.of("missing.properties")
            .map(ClassLoader::getSystemResource)
            .filter(url -> url != null)
            .map(new PropertiesToPropertySource())
            .flatMap(source -> source.stream())
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        
        Assert.assertTrue(properties.isEmpty());
    }
}
