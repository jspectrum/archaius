package com.netflix.archaius.api;

import com.netflix.archaius.api.ConfigManager.Key;

/**
 * Set of pre-defined configuration override layers that should be realistic for most applications
 * and tests
 */
public final class Layers {
    /**
     * Reserved for test specific configurations.  This layer should only be installed for unit
     * tests and will take precedence over any other layer
     */
    public static final Key TEST                  = Key.of("test",        100);
    
    /**
     * Layer reserved for code override and is normally attached to a settable config
     */
    public static final Key OVERRIDE              = Key.of("override",    200);

    /**
     * Layer with immutable system properties (-D)
     */
    public static final Key SYSTEM                = Key.of("sys",         300);
    
    /**
     * Layer with immutable environment properties
     */
    public static final Key ENVIRONMENT           = Key.of("env",         400);
    
    /**
     * Layer into which default environment properties may be loaded.  Legacy code include
     *
     */
    public static final Key ENVIRONMENT_DEFAULTS = Key.of("default",     500);

    /**
     * Layer reserved for remove configuration overrides from persistent storage
     */
    public static final Key REMOTE_OVERRIDE       = Key.of("remote",      600);
    
    /**
     * Override for application configuration loaded in property files.
     */
    public static final Key APPLICATION_OVERRIDE  = Key.of("app_override",700);

    /**
     * Layer to be used by the application for application specific configurations
     * and allows for the application to override any configuration loaded by libraries
     */
    public static final Key APPLICATION           = Key.of("app",         800);
    
    /**
     * Layer into which any class or 'library' may load its configuration
     */
    public static final Key LIBRARIES             = Key.of("libraries",   900);
}
