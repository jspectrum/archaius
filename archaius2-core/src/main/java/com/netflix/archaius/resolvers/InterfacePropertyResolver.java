package com.netflix.archaius.resolvers;

import java.lang.reflect.Type;
import java.util.Optional;

import com.netflix.archaius.api.ValueResolver;
import com.netflix.archaius.api.PropertySource;

public class InterfacePropertyResolver implements ValueResolver {

    @Override
    public <T> Optional<T> resolve(PropertySource source, String key, Type type, ValueResolver resolver) {
        // TODO Auto-generated method stub
        return null;
    }

}
