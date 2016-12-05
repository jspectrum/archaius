package com.netflix.archaius.sources;

import org.junit.Test;

import com.netflix.archaius.api.OrderedKey;

public class OrderedPropertySourceTest {
    @Test
    public void testDefault() {
        
    }
    
    @Test
    public void testOverride() {
        OrderedPropertySource source = new OrderedPropertySource("test");
        source.addPropertySource(OrderedKey.of("test", 2), ImmutablePropertySource.builder()
                .put("foo.bar", "bar")
                .build());
        source.addPropertySource(OrderedKey.of("override", 1), ImmutablePropertySource.builder()
                .put("foo.bar", "override")
                .build());
        
        
        source.stream()
            .forEach(entry -> System.out.println("1:" + entry.getKey() + " = " + entry.getValue()));
        
        source.stream("foo")
            .forEach(entry -> System.out.println("1:" + entry.getKey() + " = " + entry.getValue()));
    }
    
    @Test
    public void testNotification() {
        MutablePropertySource mutable = new MutablePropertySource("settable");
        mutable.setProperty("foo.bar", "override");
        
        OrderedPropertySource source = new OrderedPropertySource("test");
        source.addPropertySource(OrderedKey.of("test", 2), ImmutablePropertySource.builder()
                .put("foo.bar", "bar")
                .build());
        source.addPropertySource(OrderedKey.of("override", 1), mutable);
        
        source.addListener((s) -> System.out.println("Update"));
        mutable.setProperty("foo.bar", "update");
    }
}
