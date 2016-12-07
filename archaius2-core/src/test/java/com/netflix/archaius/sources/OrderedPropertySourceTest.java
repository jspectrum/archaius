package com.netflix.archaius.sources;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.archaius.api.Layers;
import com.netflix.archaius.api.PropertySource;

public class OrderedPropertySourceTest {
    private static final PropertySource override = ImmutablePropertySource.builder()
            .named("override")
            .put("foo.bar", "override")
            .build();
    
    private static final PropertySource application = ImmutablePropertySource.builder()
            .named("application")
            .put("foo.bar", "application")
            .build();
    
    private static final PropertySource lib1 = ImmutablePropertySource.builder()
            .named("lib")
            .put("foo.bar", "lib1")
            .put("default.other", "default")
            .put("default.bar", "default")
            .build();

    private static final PropertySource lib2 = ImmutablePropertySource.builder()
            .named("lib")
            .put("foo.bar", "lib2")
            .build();
    
    @Test
    public void emptySources() {
        OrderedPropertySource source = new OrderedPropertySource("root");
        
        Assert.assertFalse(source.getProperty("foo").isPresent());
        Assert.assertTrue( source.isEmpty());
        Assert.assertTrue( source.getPropertyNames().isEmpty());
        Assert.assertEquals("root", source.getName());
        
        Assert.assertEquals(0, source.children().count());
        Assert.assertEquals(0, source.stream().count());
        Assert.assertEquals(0, source.stream("prefix").count());
        Assert.assertEquals(0, source.flattened().count());
        Assert.assertEquals(0, source.namespaced("n1", "ns2").count());
    }
    
    @Test
    public void override() {
        OrderedPropertySource source = new OrderedPropertySource("root");
        
        source.addPropertySource(Layers.LIBRARIES, lib1);
        Assert.assertEquals("lib1", source.getProperty("foo.bar").get());

        source.addPropertySource(Layers.LIBRARIES, lib2);
        Assert.assertEquals("lib2", source.getProperty("foo.bar").get());
        
        source.addPropertySource(Layers.APPLICATION, application);
        Assert.assertEquals("application", source.getProperty("foo.bar").get());
        
        source.addPropertySource(Layers.OVERRIDE, override);
        Assert.assertEquals("override", source.getProperty("foo.bar").get());
    }

    @Test
    public void namespaced() {
        OrderedPropertySource source = new OrderedPropertySource("root");
        source.addPropertySource(Layers.LIBRARIES, lib1);
        source.addPropertySource(Layers.LIBRARIES, lib2);
        source.addPropertySource(Layers.APPLICATION, application);
        source.addPropertySource(Layers.OVERRIDE, override);
        
        Assert.assertEquals(
            Arrays.asList("other=default", "bar=override"), 
            source.namespaced("foo", "default")
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList()));
    }
    
    @Test
    public void testNotification() {
        MutablePropertySource mutable = new MutablePropertySource("settable");
        
        OrderedPropertySource source = new OrderedPropertySource("test");
        source.addPropertySource(Layers.APPLICATION, application);
        source.addPropertySource(Layers.OVERRIDE, mutable);
        
        Assert.assertEquals("lib1", source.getProperty("foo.bar").get());
        
        source.addListener((s) -> System.out.println("Update"));
        
        mutable.setProperty("foo.bar", "override");
    }
}
