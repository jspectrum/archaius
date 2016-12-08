package com.netflix.archaius.api;

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
