package com.netflix.config.sources;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.config.api.PropertySource;

public class InterpolatingPropertySource extends DelegatingPropertySource {

    private final PropertySource delegate;
    private final Function<Object, Object> interpolator;
    
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
    protected PropertySource delegate() {
        return delegate;
    }
    
    @Override
    public Optional<Object> getProperty(String key) {
        return delegate().getProperty(key).map(interpolator);
    }
    
    @Override
    public PropertySource snapshot() {
        return new InterpolatingPropertySource(delegate.snapshot());
    }

    static Function<Entry<String, Object>, Entry<String, Object>> interpolate(Function<Object, Object> interpolator) {
        return entry -> new Entry<String, Object>() {
            @Override
            public String getKey() { 
                return entry.getKey(); 
            }
       
            @Override
            public Object getValue() { 
                return interpolator.apply(entry.getValue()); 
            }

            @Override
            public Supplier<Object> setValue(Object value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
