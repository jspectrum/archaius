package com.netflix.archaius.resolvers;

import java.util.TreeMap;

public class SortedMapTypeResolver extends MapTypeResolver {
    SortedMapTypeResolver() {
        super(TreeMap::new);
    }
}
