package com.netflix.archaius.sources;

import java.util.HashMap;
import java.util.Map;

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
            .named("lib1")
            .put("foo.bar", "lib1")
            .put("default.other", "default")
            .put("default.bar", "default")
            .build();

    private static final PropertySource lib2 = ImmutablePropertySource.builder()
            .named("lib2")
            .put("foo.bar", "lib2")
            .build();
    
    @Test
    public void emptySources() {
        LayeredPropertySource source = new LayeredPropertySource("root");
        
        Assert.assertFalse(source.getProperty("foo").isPresent());
        Assert.assertTrue( source.isEmpty());
        Assert.assertTrue( source.getKeys().isEmpty());
        Assert.assertEquals("root", source.getName());
        
        Assert.assertEquals(0, source.children().count());
        Assert.assertEquals(0, source.size());
        Assert.assertEquals(0, source.getKeys("prefix").size());
//        Assert.assertEquals(0, source.flattened().count());
//        Assert.assertEquals(0, source.fallbacks("n1", "ns2").count());
    }
    
    @Test
    public void override() {
        LayeredPropertySource source = new LayeredPropertySource("root");
        
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
        LayeredPropertySource source = new LayeredPropertySource("root");
        source.addPropertySource(Layers.LIBRARIES, lib1);
        source.addPropertySource(Layers.LIBRARIES, lib2);
        source.addPropertySource(Layers.APPLICATION, application);
        source.addPropertySource(Layers.OVERRIDE, override);
        
//        Assert.assertEquals(
//            Arrays.asList("other=default", "bar=override"), 
//            source.fallbacks("foo", "default")
//                .map(entry -> entry.getKey() + "=" + entry.getValue())
//                .collect(Collectors.toList()));
    }
    
    @Test
    public void testNotification() {
        MutablePropertySource mutable = new MutablePropertySource("settable");
        
        LayeredPropertySource source = new LayeredPropertySource("test");
        source.addPropertySource(Layers.APPLICATION, application);
        source.addPropertySource(Layers.OVERRIDE, mutable);
        
        Assert.assertEquals("lib1", source.getProperty("foo.bar").get());
        
        source.addListener((s) -> System.out.println("Update"));
        
        mutable.setProperty("foo.bar", "override");
    }
    
    @Test
    public void listSources() {
        LayeredPropertySource source = new LayeredPropertySource("root");
        source.addPropertySource(Layers.LIBRARIES, lib1);
        source.addPropertySource(Layers.LIBRARIES, lib2);
        source.addPropertySource(Layers.APPLICATION, application);
        source.addPropertySource(Layers.OVERRIDE, override);

        Map<String, Object> sources = new HashMap<>();
        source
            .flattened()
            .forEach(s -> s.getProperty("foo.bar")
                .ifPresent(value -> sources.put(s.getName(), value)));
        
        System.out.println(sources);;
    }
}
