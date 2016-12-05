package com.netflix.archaius.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
     * @param key
     * @return Value for the property 
     */
    Optional<?> getProperty(String key);
    
    Stream<Map.Entry<String, Object>> stream();
    
    Stream<Map.Entry<String, Object>> stream(String prefix);
    
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
     * Add a listener that is invoked for any updates to the PropertySource.  To avoid complexity
     * for figuring out what exactly changes, especially when dealing with interpolation the notification
     * mechanism simply informs the listener that something had changed.
     * @param listener
     */
    default Runnable addListener(Consumer<PropertySource> listener) { return () -> {}; }
}
