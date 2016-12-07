package com.netflix.archaius.sources.yaml;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class YamlTest {
    @Test
    public void loadExisting() {
        Map<String, Object> properties = Stream.of("loadtest.yml")
            .map(ClassLoader::getSystemResource)
            .map(new YamlToPropertySource())
            .flatMap(source -> source.stream())
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        
        Assert.assertEquals(10,  properties.size());
    }
    
    @Test
    public void ignoreMissingFile() {
        Map<String, Object> properties = Stream.of("missing.yml")
            .map(ClassLoader::getSystemResource)
            .filter(url -> url != null)
            .map(new YamlToPropertySource())
            .flatMap(source -> source.stream())
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        
        Assert.assertTrue(properties.isEmpty());
    }
}
