package com.netflix.archaius;

import com.netflix.archaius.api.ConfigNode;
import com.netflix.archaius.api.Decoder;

import java.util.Iterator;
import java.util.SortedMap;

/**
 * ConfigNode backed by a SortedMap.  Access to the map is synchronized
 *
 */
public class SortedMapChildNode implements ConfigNode {
    private final SortedMap<String, ConfigNode> values;
    
    // Prefix without trailing '.'
    private final String path;

    private final Decoder decoder;

    public SortedMapChildNode(SortedMap<String, ConfigNode> values, Decoder decoder, String path) {
        this.values = values.subMap(path + ".", path + ".\uffff");
        this.path = path;
    }
    
    @Override
    public ConfigNode child(String name) { 
        return new SortedMapChildNode(values, this.path + "." + name) {
            
        };
    }
    
    @Override
    public Object value() { 
        return values.get(path); 
    }
    
    /**
     * @param key
     * @return True if the key is contained within this or any of it's child configurations
     */
    @Override
    public boolean containsKey(String key) { 
        return values.containsKey(this.path + "." + key); 
    }
    
    @Override
    public boolean isEmpty() { 
        return values.isEmpty(); 
    }
    
    @Override
    public Iterable<String> keys() {
        return values.keySet();
    }

    @Override
    public ConfigNode root() {
        return this;
    }
}
