package com.netflix.archaius.api;

public interface TypeDecoder<T> {
    T decode(TypeToken type, ConfigNode node, TypeDecoders decoders);
}
