package com.netflix.archaius.sources;

import java.util.Optional;
import java.util.function.Function;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;

public class InterpolatingPropertySource extends DelegatingPropertySource {

    private final Function<Object, Object> interpolator;
    private final PropertySource delegate;
    
    public InterpolatingPropertySource(PropertySource delegate) {
        this.delegate = delegate;
        
        StrInterpolator.Lookup lookup = key -> delegate.getProperty(key).map(Object::toString).orElse(null);
        this.interpolator = value -> {
            if (value.getClass() == String.class) {
                return CommonsStrInterpolator.INSTANCE.create(lookup).resolve((String)value);
            }
            return value;
        };
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return super.getProperty(name).map(interpolator);
    }

    @Override
    public PropertySource subset(String prefix) {
        return new InterpolatingPropertySource(delegate().subset(prefix)) {
            public PropertySource subset(String childPrefix) {
                return new InterpolatingPropertySource(delegate().subset(prefix + "." + childPrefix)); 
            }
        };
    }

    @Override
    protected PropertySource delegate() {
        return delegate;
    }
}
