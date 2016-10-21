package com.netflix.archaius.api;

import java.lang.reflect.Type;

public interface TypeDecoders {

    <T> T decode(TypeToken type, ConfigNode node);
    
    default <T> T decode(Type type, ConfigNode node) {
        return decode(TypeToken.create(type), node);
    }
    
    default <T> T decode(Class<T> type, ConfigNode node) {
        return decode(TypeToken.create(type), node);
    }

    <T> TypeDecoder<T> getTypeDecoder(TypeToken type);

    default <T> TypeDecoder<T> getTypeDecoder(Type type) {
        return getTypeDecoder(TypeToken.create(type));
    }
    
}
