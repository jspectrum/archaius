package com.netflix.archaius;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.netflix.archaius.PropertySourceConfiguration;
import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.archaius.api.annotations.PropertyName;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.ImmutablePropertySource;

public class PropertySourceBasedConfigurationTest {
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
        
        PropertySourceConfiguration configuration = new PropertySourceConfiguration(source);
        Foo foo = configuration.get("foo", Foo.class).get();

//        foo.onString((newValue) -> do something);
        
        System.out.println(foo);
    }
    
}
