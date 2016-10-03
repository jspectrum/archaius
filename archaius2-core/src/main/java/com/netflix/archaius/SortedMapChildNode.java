package com.netflix.archaius;

import com.netflix.archaius.api.DataNode;

import java.util.Iterator;
import java.util.SortedMap;

public class SortedMapChildNode implements DataNode {
    private final SortedMap<String, DataNode> values;
    
    // Prefix without trailing '.'
    private final String path;

    private final DataNode root;

    public SortedMapChildNode(DataNode root, SortedMap<String, DataNode> values, String path) {
        this.values = values.subMap(path + ".", path + ".\uffff");
        this.path = path;
        this.root = root;
    }
    
    @Override
    public DataNode child(String name) { 
        return new SortedMapChildNode(root, values, this.path + "." + name);
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
    public Iterator<String> getKeys() { 
        return values.keySet().iterator();
    }

    @Override
    public DataNode root() {
        return root;
    }
}
