package com.netflix.archaius.sources.yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class YamlTest {
    @Test
    public void loadExisting() {
        Map<String, Object> properties = new HashMap<>();
        Stream.of("loadtest.yml")
            .map(ClassLoader::getSystemResource)
            .map(new YamlToPropertySource())
            .forEach(source -> source.forEach((key, value) -> properties.put(key, value)));
        
        Assert.assertEquals(10,  properties.size());
    }
    
    @Test
    public void ignoreMissingFile() {
        Map<String, Object> properties = new HashMap<>();
        Stream.of("missing.yml")
            .map(ClassLoader::getSystemResource)
            .filter(url -> url != null)
            .map(new YamlToPropertySource())
            .forEach(source -> source.forEach((key, value) -> properties.put(key, value)));
        
        Assert.assertTrue(properties.isEmpty());
    }
}
