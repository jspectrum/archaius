package com.netflix.archaius.api;

import java.net.URI;

public interface PropertySourceFactory {
    PropertySource create(URI uri);
}
