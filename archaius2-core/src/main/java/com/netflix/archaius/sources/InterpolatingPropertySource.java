package com.netflix.archaius.sources;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    public Stream<Entry<String, Supplier<Object>>> stream() {
        return delegate().stream().map(PropertySourceUtils.interpolate(interpolator));
    }

    @Override
    public Stream<Entry<String, Supplier<Object>>> stream(String prefix) {
        if (!prefix.endsWith(".")) {
            return stream(prefix + ".");
        } else {
            return delegate().stream(prefix)
                .map(PropertySourceUtils.interpolate(interpolator));
        }
    }


    @Override
    public Optional<Object> getProperty(String name) {
        return super.getProperty(name).map(interpolator);
    }

    @Override
    protected PropertySource delegate() {
        return delegate;
    }
}
