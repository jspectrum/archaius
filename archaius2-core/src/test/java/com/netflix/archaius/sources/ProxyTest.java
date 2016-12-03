package com.netflix.archaius.sources;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.netflix.archaius.api.CreatorFactory;
import com.netflix.archaius.creator.CreatorFactoryBuilder;
import com.netflix.archaius.creator.ProxyTypeCreator;

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
//                .put("foo.map", "a=1,b=2,c=3")
                .put("foo.map.a1", "1")
                .put("foo.map.a2", "2")
                .put("foo.map.a3", "3")
                .build());
        
        CreatorFactory factory = new CreatorFactoryBuilder().build();
        
        source.forEach("foo", (k, v) -> System.out.println(k + " = " + v));
        
        Foo foo = source.collect("foo", new ProxyTypeCreator<Foo>(factory, Foo.class, Foo.class.getAnnotations()));
        System.out.println(foo);
    }
}
