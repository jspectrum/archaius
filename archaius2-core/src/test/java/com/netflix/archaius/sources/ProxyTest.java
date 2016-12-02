package com.netflix.archaius.sources;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ProxyTest {
    public static interface Foo {
        String getString();
        Integer getInteger();
        default Boolean getBoolean() { return false; }
        List<String> getList();
        Map<String, Integer> getMap();
    }
    
    @Test
    public void test() {
        ResolvingPropertySource source = new ResolvingPropertySource(
            ImmutablePropertySource.builder()
                .put("foo.string",  "a1")
                .put("foo.integer", "2")
                .put("foo.list", "a,b,c")
                .put("foo.map", "a=1,b=2,c=3")
                .put("foo.map.a1", "1")
                .put("foo.map.a2", "2")
                .put("foo.map.a3", "3")
                .build());
        
        
        Foo foo = source.get("foo", Foo.class).get();
            
        System.out.println(foo);
        
    }
}
