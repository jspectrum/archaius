package com.netflix.archaius.sources;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class PropertySourceUtils {
    public static UnaryOperator<Entry<String, Object>> stripPrefix(String prefix) {
        return entry -> new Entry<String, Object>() {
            String key = entry.getKey().substring(prefix.length());
            
            @Override
            public String getKey() { 
                return key; 
            }
       
            @Override
            public Object getValue() { 
                return entry.getValue(); 
            }
       
            @Override
            public Object setValue(Object value) {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    public static Function<Entry<String, Object>, Entry<String, Supplier<Object>>> interpolate(Function<Object, Object> interpolator) {
        return entry -> new Entry<String, Supplier<Object>>() {
            @Override
            public String getKey() { 
                return entry.getKey(); 
            }
       
            @Override
            public Supplier<Object> getValue() { 
                return () -> interpolator.apply(entry.getValue()); 
            }

            @Override
            public Supplier<Object> setValue(Supplier<Object> value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
