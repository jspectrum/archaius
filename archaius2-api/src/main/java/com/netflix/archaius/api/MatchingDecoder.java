package com.netflix.archaius.api;

/**
 * Decoder extension SPI through which custom Decoders may be applied to types not known to 
 * archaius.
 * 
 * @see DefaultDecoder
 */
public interface MatchingDecoder extends Decoder {
    /**
     * Returns true if this decoder can decode this type.
     * @param type The type being decoded
     * @param encoded The raw encoded string
     * @return True if this decoder can decode the type.
     */
    <T> boolean matches(Class<T> type, String encoded);
}
