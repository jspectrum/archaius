package com.netflix.archaius.sources;

import java.lang.reflect.Array;

import org.junit.Test;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.config.PropertySourceConfig;

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
        
        ResolvingPropertySource resolvingSource = new ResolvingPropertySource(new InterpolatingPropertySource(source));
        
        PropertySourceConfig config = new PropertySourceConfig(resolvingSource);
        
//        System.out.println(config.getString("e"));
//        System.out.println(config.getDouble("a"));
        
//        Integer[] ar = resolvingSource.get(Integer[].class, "d").get();
//        System.out.println(Arrays.asList(resolvingSource.get(Integer[].class, "d").get()));
//        int[] ar2 = resolvingSource.get(int[].class, "d").get();
        boolean[] b = (boolean[]) resolvingSource.get("bool", boolean[].class).get();
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
        
        ResolvingPropertySource resolvingSource = new ResolvingPropertySource(source);
        resolvingSource.forEach((key, value) -> System.out.println("1:" + key + "=" + value));
        System.out.println(resolvingSource.getPropertyNames());
    }
}
