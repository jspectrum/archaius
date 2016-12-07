package com.netflix.archaius.sources;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import org.junit.Test;

import com.netflix.archaius.PropertySourceBasedConfiguration;
import com.netflix.archaius.api.CreatorFactory;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.archaius.api.annotations.PropertyName;
import com.netflix.archaius.creator.CreatorFactoryBuilder;
import com.netflix.archaius.creator.ProxyTypeCreator;
import com.netflix.archaius.sources.ImmutablePropertySource;

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
        
        CreatorFactory factory = new CreatorFactoryBuilder().build();
        
//        source.stream("foo").forEach(t -> System.out.println(t.getKey() + " = " + t.getValue()));

        PropertySourceBasedConfiguration config = new PropertySourceBasedConfiguration(source);
        
//        config.stream().forEach(t -> System.out.println("Config: " + t.getKey() + " = " + t.getValue().get()));
        
        Foo foo = config.stream("foo").collect(Collector.of(
                () -> new ProxyTypeCreator<Foo>(factory, Foo.class, Foo.class.getAnnotations()),
                (creator, entry) -> creator.accept(entry.getKey(), entry.getValue()),
                (c1, c2) -> c1, 
                ProxyTypeCreator::get));

        System.out.println(foo);
    }
}
