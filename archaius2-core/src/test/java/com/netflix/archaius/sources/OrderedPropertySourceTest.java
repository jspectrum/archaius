package com.netflix.archaius.sources;

import org.junit.Test;

import com.netflix.archaius.api.OrderedKey;

public class OrderedPropertySourceTest {
    @Test
    public void testDefault() {
        
    }
    
    @Test
    public void testOverride() {
        OrderedPropertySource source = new OrderedPropertySource("root");
        source.addPropertySource(OrderedKey.of("override", 1), ImmutablePropertySource.builder()
                .named("override")
                .put("foo.bar", "override")
                .build());
        source.addPropertySource(OrderedKey.of("test", 2), ImmutablePropertySource
                .builder()
                .named("test")
                .put("foo.bar", "bar")
                .build());
        source.addPropertySource(OrderedKey.of("lib", 3), ImmutablePropertySource.builder()
                .named("lib")
                .put("lib", "value")
                .put("foo.bar", "lib")
                .put("default.other", "lib")
                .put("default.bar", "lib")
                .build());
        
//        source.stream()
//            .forEach(entry -> System.out.println("1:" + entry.getKey() + " = " + entry.getValue()));
//        
//        source.stream("foo")
//            .forEach(entry -> System.out.println("1:" + entry.getKey() + " = " + entry.getValue()));
//        
//        source.children().collect(SourcePrinter::new, SourcePrinter::onPropertySource, SourcePrinter::finish);
        
        source.namespaced("foo", "default")
            .forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));
        
//        List<String> override = source.flattened()
//            .filter(s -> s.getProperty("foo.bar").isPresent())
//            .collect(ArrayList::new, 
//                    (list, s) -> list.add(s.getName() + " : " + s.getProperty("foo.bar").get()),
//                    ArrayList::addAll)
//            ;
//        
        
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
