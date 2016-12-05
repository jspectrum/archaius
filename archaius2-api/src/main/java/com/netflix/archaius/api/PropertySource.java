package com.netflix.archaius.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
    Optional<Object> getProperty(String key);
    
    Stream<Map.Entry<String, Supplier<Object>>> stream();
    
    Stream<Map.Entry<String, Supplier<Object>>> stream(String prefix);
    
    /**
     * Iterate through all properties of the PropertySource and their values.  
     * @param consumer
     */
    default void forEach(BiConsumer<String, Object> consumer) {
        stream().forEach(entry -> consumer.accept(entry.getKey(), entry.getValue().get()));
    }
    
    /**
     * Iterate through all properties starting with the specified prefix.  Note
     * that property names passed to the consumer do not include the prefix.
     * 
     * @param prefix
     * @param consumer
     */
    default void forEach(String prefix, BiConsumer<String, Object> consumer) {
        stream(prefix).forEach(entry -> consumer.accept(entry.getKey(), entry.getValue().get()));
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
    default Runnable addListener(Consumer<PropertySource> listener) { return () -> {}; }
}
