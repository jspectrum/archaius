package com.netflix.archaius;

import com.netflix.archaius.config.MapConfig;

import org.junit.Assert;
import org.junit.Test;

public class DefaultTypeDecodersTest {
    @Test
    public void test() {
        MapConfig config = MapConfig.builder()
            .put("foo", "123")
            .build()
            ;
        
        DefaultTypeDecoders decoders = DefaultTypeDecoders.builder().build();
        
        Assert.assertEquals(123, decoders.decode(Integer.class, config.child("foo")).intValue());
    }
}
