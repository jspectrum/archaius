package com.netflix.archaius.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contract for a source of raw properties.  A PropertySource may be associated with a single
 * data source or be a composite of multiple PropertySource instances.  Values may be any object
 * including strings, boxed primitives, collections, pojos, etc.
 * 
 * PropertySource internals are exposed via a set of convenience streaming APIs for common use cases.
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
     * Register a consumer that will be invoked for any updates to the PropertySource.  To avoid the complexity
     * of figuring out what exactly changed, especially when dealing with interpolation, the notification
     * mechanism simply informs the listener that something had changed.
     * @param listener
     * @return Runnable that unregisters the listener when its run() method is called. 
     */
    default Runnable addListener(Consumer<PropertySource> listener) { return () -> {}; }
    
    /**
     * @return Stream of all entries of this PropertySource
     */
    Stream<Map.Entry<String, Object>> stream();
    
    /**
     * @return Stream of all entries of this PropertySource
     */
    Stream<Map.Entry<String, Object>> stream(String prefix);

    /**
     * @return Stream with any child PropertySource instances used to construct this property source.
     * For most PropertySource implementations this will be an empty stream.
     */
    default Stream<PropertySource> children() {
        return Stream.empty();
    }

    /**
     * Use this to stream properties from overriding namespaces of properties.  
     * 
     * For example, let's say an RPC client has properties with a named prefix but can default to properties
     * with a 'default' prefix, 
     * 
     * {@code
     *  client1.timeout=200
     *  default.timeout=100
     *  default.retries=3
     * }
     * 
     * The following,
     * 
     * {@code 
     *  source.namedspaced("client1", "default") 
     * }
     *  
     * will stream the properties
     *  
     * {@code 
     *  client1.timeout=200
     *  default.retries=3
     * }
     * 
     * Properties will streamed in namespace order where all the properties from each 
     * namespace are grouped together.
     * 
     * @param namespaces
     * @return Stream of unique entries from first seen namespace
     */
    default Stream<Map.Entry<String, Object>> namespaced(String... namespaces) {
        Set<String> seen = new HashSet<>();
        return Stream.of(namespaces)
            .flatMap(this::stream)
            .filter(entry -> seen.add(entry.getKey()))
            ;
    }

    /**
     * @return Flattened view of the child PropertySource instances backing this PropertySource.  The
     * resulting stream returns an ordered set of PropertySources.
     */
    default Stream<PropertySource> flattened() {
        return Stream.concat(Stream.of(this), children().flatMap(PropertySource::flattened));
    }

    default Stream<String> keys(String prefix) {
        return stream(prefix).map(entry -> entry.getKey());
    }
    
    /**
     * Trace the sources that contain the request properties and return a map of source name to value
     * in override order.
     * 
     * @param propertyName
     * @return
     */
    default Map<String, Object> trace(String propertyName) {
        return flattened()
            .flatMap(s -> Collections.singletonMap(s, s.getProperty(propertyName)).entrySet().stream())
            .filter(entry -> entry.getValue().isPresent())
            .collect(Collectors.toMap(
                entry -> entry.getKey().getName(), 
                entry -> entry.getValue().get(), 
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                }, 
                LinkedHashMap::new));
    }
}
