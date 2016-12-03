package com.netflix.archaius.sources;

import org.junit.Test;

import com.netflix.archaius.BiConsumers;
import com.netflix.archaius.api.OrderedKey;

public class OrderedPropertySourceTest {
    @Test
    public void testDefault() {
        
    }
    
    @Test
    public void testOverride() {
        OrderedPropertySource source = new OrderedPropertySource("test");
        source.addPropertySource(OrderedKey.of("test", 1), ImmutablePropertySource.builder()
                .put("foo", "bar")
                .build());
        source.addPropertySource(OrderedKey.of("override", 1), ImmutablePropertySource.builder()
                .put("foo", "override")
                .build());
        
        source.forEach(BiConsumers.print(System.out));
    }
    
    @Test
    public void testNotification() {
        
    }
    
    @Test
    public void testPerfix() {
        
    }
}
