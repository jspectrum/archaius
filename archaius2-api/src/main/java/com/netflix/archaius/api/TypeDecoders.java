package com.netflix.archaius.api;

import java.lang.reflect.Type;

public interface TypeDecoders {

    <T> T decode(TypeToken type, DataNode node);
    
    default <T> T decode(Type type, DataNode node) {
        return decode(TypeToken.create(type), node);
    }
    
    default <T> T decode(Class<T> type, DataNode node) {
        return decode(TypeToken.create(type), node);
    }

    <T> TypeDecoder<T> getTypeDecoder(TypeToken type);

    default <T> TypeDecoder<T> getTypeDecoder(Type type) {
        return getTypeDecoder(TypeToken.create(type));
    }
    
}
