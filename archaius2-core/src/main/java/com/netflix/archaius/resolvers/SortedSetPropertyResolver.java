package com.netflix.archaius.resolvers;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.ValueResolver;

public class SortedSetPropertyResolver extends SetPropertyResolver {
    SortedSetPropertyResolver() {
        super(TreeSet::new);
    }
}
