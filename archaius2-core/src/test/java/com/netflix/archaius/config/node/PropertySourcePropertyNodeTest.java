package com.netflix.archaius.config.node;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.netflix.archaius.api.PropertyNode;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.ResolverLookup;
import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.archaius.api.annotations.PropertyName;
import com.netflix.archaius.node.ResolverLookupImpl;
import com.netflix.archaius.node.PropertySourcePropertyNode;
import com.netflix.archaius.sources.ImmutablePropertySource;

public class PropertySourcePropertyNodeTest {
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
                .put("foo.map.a3", "3")
                .build();
        
        PropertyNode node = new PropertySourcePropertyNode(source).getNode("foo");
        
        ResolverLookup lookup = new ResolverLookupImpl();
        
        Foo foo = lookup.get(Foo.class).resolve(node, lookup);

        System.out.println(foo);
    }
}
