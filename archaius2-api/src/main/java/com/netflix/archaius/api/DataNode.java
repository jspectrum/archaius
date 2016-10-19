package com.netflix.archaius.api;

import java.util.Iterator;

public interface DataNode {
    /**
     * Return a child node with any properties prefixed by 'prefix'
     * @param name
     * @return
     */
    DataNode child(String name);
    
    /**
     * Get the property from the Decoder.  All basic data types as well any type
     * will a valueOf or String contructor will be supported.
     * @param type
     * @param key
     * @return
     */
    Object value();
    
    /**
     * @param key
     * @return True if the key is contained within this or any of it's child configurations
     */
    boolean containsKey(String key);
    
    /**
     * @return True if empty or false otherwise.
     */
    boolean isEmpty();
    
    /**
     * @return Return an iterator to all property names owned by this config
     */
    Iterator<String> getKeys();
    
    /**
     * @return Return the root node 
     */
    DataNode root();
}
