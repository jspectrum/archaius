package com.netflix.archaius.api;

public interface ConfigNode {
    /**
     * Return a child node with any properties prefixed by 'prefix'
     * 
     * @param name
     * @return
     */
    ConfigNode child(String name);
    
    /**
     * For scalar nodes return the property value
     * 
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
     * @return Return an iterable of all property names owned by this node.  If a prefix node
     * then all prefixes will be stripped.
     */
    Iterable<String> keys();
    
    ConfigNode root();
}
