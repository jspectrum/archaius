package com.netflix.config.api;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public interface PropertyNode {
    /**
     * Contract for resolving a type from a PropertyNode.
     * @param <T>
     */
    public interface Resolver<T> {
        
        /**
         * 
         * @param node Node in the property tree from where the value will be resolved.  This could be a leaf
         *  node for single value types or an inner node for Maps and complex types.
         * @param context Resolve 
         * @return
         */
        T resolve(PropertyNode node, ResolverLookup context);
    }

    /**
     * The ResolverLookup contains the logic to determine the Resolver for a type.
     */
    public interface ResolverLookup {

        <T> Resolver<T> get(Type type);

        default <T> Resolver<T> get(Class<T> type) {
            return get((Type)type);
        }
        
    }

    default Optional<?> getValue() {
        return Optional.empty();
    }
    
    default PropertyNode getChild(String key) {
        return new PropertyNode() {};
    }
    
    default Collection<String> children() {
        return Collections.emptyList();
    }
}
