package com.netflix.archaius.api;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface TypeCreator<T> extends BiConsumer<String, Supplier<Object>>, Supplier<T> {
}

