package com.netflix.archaius.resolvers;

import java.util.TreeMap;

public class SortedMapPropertyResolver extends MapPropertyResolver {
    SortedMapPropertyResolver() {
        super(TreeMap::new);
    }
}
