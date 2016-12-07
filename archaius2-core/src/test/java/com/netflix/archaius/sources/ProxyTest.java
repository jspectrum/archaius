package com.netflix.archaius.sources;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.netflix.archaius.PropertySourceBasedConfiguration;
import com.netflix.archaius.api.CreatorRegistry;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.archaius.api.annotations.PropertyName;
import com.netflix.archaius.creator.CreatorRegistryBuilder;

public class ProxyTest {
    public static interface Foo {
        String getString();
        
        Integer getInteger();

        @DefaultValue("50")
        Integer getDefaultInteger();
        
        default Boolean getBoolean() { return false; }
        
        @PropertyName(name = "list")
        List<String> getListAbc();
        
        Map<String, Integer> getMap();
    }
    
    @Test
    public void test() {
        PropertySource source = ImmutablePropertySource.builder()
                .put("value", "30")
                .put("foo.string",  "a1")
                .put("foo.integer", "2")
                .put("foo.list", "a,b,c")
//                .put("foo.map", "a=1,b=2,c=3")
                .put("foo.map.a1", "1")
                .put("foo.map.a2", "2")
                .put("foo.map.a3", "${value}")
                .build();
        
        CreatorRegistry registry = new CreatorRegistryBuilder().build();
        
        PropertySourceBasedConfiguration config = new PropertySourceBasedConfiguration(source);
        
        Foo foo = config.stream("foo").collect(registry.create(Foo.class).toCollector());

        System.out.println(foo);
    }
}
