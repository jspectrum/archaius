package com.netflix.archaius.sources;

import org.junit.Test;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.config.PropertySourceConfig;

public class SourcesTest {
    @Test
    public void test() {
        PropertySource source = MapPropertySource.builder()
                .put("a", "${b}")
                .put("b", 1.5)
                .put("d", "1,2,3,4")
                .build();
        
        PropertySourceConfig config = new PropertySourceConfig(new ResolvingPropertySource(source));
        
        System.out.println(config.getString("a"));
        System.out.println(config.getDouble("a"));
    }
}
