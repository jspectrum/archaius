package com.netflix.archaius.api;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Raw source for properties identified by a string name.  Values may be any object
 * including strings and primitives.
 */
public interface PropertySource {
    /**
     * @return Name of the property source.  This could be an arbitrary name or a file name from whence the
     * configuration was loaded
     */
    String getName();

    /**
     * @param name
     * @return Value for the property 
     */
    Optional<Object> getProperty(String name);
    
    /**
     * Iterate through all properties of the PropertySource and their values.  
     * @param consumer
     */
    void forEach(BiConsumer<String, Object> consumer);
    
    /**
     * @return Immutable collection of all property names.  For dynamic PropertySources it's still possible
     * for a property name in this collection to no longer exist when getProperty is called.
     */
    Collection<String> getPropertyNames();

    /**
     * @return True if there are no properties in the PropertySource
     */
    boolean isEmpty();
    
    PropertySource subset(String prefix);
}
