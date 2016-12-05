package com.netflix.archaius.sources;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class PropertySourceUtils {
    static UnaryOperator<Entry<String, Supplier<Object>>> stripPrefix(String prefix) {
        return entry -> new Entry<String, Supplier<Object>>() {
            String key = entry.getKey().substring(prefix.length());
            
            @Override
            public String getKey() { 
                return key; 
            }
       
            @Override
            public Supplier<Object> getValue() { 
                return entry.getValue(); 
            }
       
            @Override
            public Supplier<Object> setValue(Supplier<Object> value) {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    static UnaryOperator<Entry<String, Supplier<Object>>> interpolate(Function<Object, Object> interpolator) {
        return entry -> new Entry<String, Supplier<Object>>() {
            @Override
            public String getKey() { 
                return entry.getKey(); 
            }
       
            @Override
            public Supplier<Object> getValue() { 
                return () -> interpolator.apply(entry.getValue().get()); 
            }
       
            @Override
            public Supplier<Object> setValue(Supplier<Object> value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
