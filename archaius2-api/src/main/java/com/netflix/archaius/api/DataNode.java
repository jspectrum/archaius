package com.netflix.archaius.api;

import java.util.Collections;
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
    default Object value() { return null; }
    
    /**
     * @param key
     * @return True if the key is contained within this or any of it's child configurations
     */
    default boolean containsKey(String key) { return false; }
    
    /**
     * @return True if empty or false otherwise.
     */
    default boolean isEmpty() { return true; }
    
    /**
     * @return Return an iterator to all property names owned by this config
     */
    default Iterator<String> getKeys() { return Collections.emptyIterator(); }
    
    DataNode root();
}
