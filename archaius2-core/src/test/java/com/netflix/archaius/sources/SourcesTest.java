package com.netflix.archaius.sources;

import java.lang.reflect.Array;

import org.junit.Test;

import com.netflix.archaius.Configuration;
import com.netflix.archaius.api.PropertySource;

public class SourcesTest {
    @Test
    public void ar() {
        Class type = int[].class;
        Array.newInstance(type.getComponentType(), 10);
    }
    
    @Test
    public void test() {
        PropertySource source = ImmutablePropertySource.builder()
                .put("a", "${b}")
                .put("b", 1.5)
                .put("d", "1,2,3,4")
                .put("bool", "true,${b1:false},false,${b1:true}")
                .put("c", "${e}")
                .put("e", "${c}")
                .build();
        
        Configuration config = new Configuration(source);
        
//        System.out.println(config.getString("e"));
//        System.out.println(config.getDouble("a"));
        
//        Integer[] ar = resolvingSource.get(Integer[].class, "d").get();
//        System.out.println(Arrays.asList(resolvingSource.get(Integer[].class, "d").get()));
//        int[] ar2 = resolvingSource.get(int[].class, "d").get();
        boolean[] b = (boolean[]) config.get("bool", boolean[].class).get();
    }
    
    @Test
    public void prefix() {
        PropertySource source = ImmutablePropertySource.builder()
            .put("a",   "a1")
            .put("b",   "a2")
            .put("b.c.d", "a3")
            .put("b.d", "a4")
            .put("e",   "a5")
            .build();
        
        Configuration config = new Configuration(source);
        
        config.stream().forEach(entry -> System.out.println("1:" + entry.getKey() + " = " + entry.getValue().get()));
    }
}
