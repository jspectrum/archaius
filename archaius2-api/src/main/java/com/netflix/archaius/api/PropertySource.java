package com.netflix.archaius.api;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    Optional<Object> getProperty(String key);
    
    /**
     * Iterate through all properties of the PropertySource and their values.  
     * @param consumer
     */
    void forEach(BiConsumer<String, Supplier<Object>> consumer);
    
    /**
     * Iterate through all properties starting with the specified prefix.  Note
     * that property names passed to the consumer do not include the prefix.
     * 
     * @param prefix
     * @param consumer
     */
    void forEach(String prefix, BiConsumer<String, Supplier<Object>> consumer);

    /**
     * Apply all properties to a TypeCreator and call it's get() method to create
     * and immutable object
     * @param creator
     * @return
     */
    default <T> T collect(TypeCreator<T> creator) {
        forEach(creator);
        return creator.get();
    }
    
    /**
     * Apply all properties prefixed with 'prefix' to a TypeCreator and call it's get() 
     * method to create and immutable object.  Note that all properties passed to the
     * creator will not have the prefix.
     * @param creator
     * @return
     */
    default <T> T collect(String prefix, TypeCreator<T> creator) {
        forEach(prefix, creator);
        return creator.get();
    }
    
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
    default Cancellation addListener(Consumer<PropertySource> listener) { return Cancellation.empty(); }

}
