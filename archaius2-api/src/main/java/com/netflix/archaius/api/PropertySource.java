package com.netflix.archaius.api;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Raw source for properties identified by a string name.  Values may be any object
 * including strings and primitives.
 */
public interface PropertySource {
    interface Listener {
        void onChanged(PropertySource propertySource);
    }
    
    /**
     * @return Name of the property source.  This could be an arbitrary name or a file name from whence the
     * configuration was loaded
     */
    String getName();

    /**
     * @param key
     * @return Value for the property 
     */
    Optional<Object> getProperty(String key);
    
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
    
    /**
     * @param prefix
     * @return PropertySource that is a subset of this PropertySource such that
     *  all properties do no have the prefix.  
     */
    PropertySource subset(String prefix);
    
    /**
     * Add a listener that is invoked for any updates to the PropertySource.  To avoid complexity
     * for figuring out what exactly changes, especially when dealing with interpolation the notification
     * mechanism simply informs the listener that something had changed.
     * @param listener
     */
    default Cancellation addListener(Listener listener) { return Cancellation.empty(); }

}
