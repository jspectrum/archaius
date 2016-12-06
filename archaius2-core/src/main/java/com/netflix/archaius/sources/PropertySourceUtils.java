package com.netflix.archaius.sources;

import java.util.Map.Entry;
import java.util.function.UnaryOperator;

final class PropertySourceUtils {
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
}
