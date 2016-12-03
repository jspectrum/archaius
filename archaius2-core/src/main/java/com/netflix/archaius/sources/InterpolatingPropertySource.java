package com.netflix.archaius.sources;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public void forEach(BiConsumer<String, Supplier<Object>> consumer) {
        delegate().forEach((k, sv) -> consumer.accept(k, () -> interpolator.apply(sv.get())));
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Supplier<Object>> consumer) {
        delegate().forEach(prefix, (k, sv) -> consumer.accept(k, () -> interpolator.apply(sv.get())));
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
