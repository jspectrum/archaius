package com.netflix.archaius.api;

/**
 * Set of pre-defined configuration override layers that should be realistic for most applications
 * and tests
 */
public final class Layers {
    /**
     * Reserved for test specific configurations.  This layer should only be installed for unit
     * tests and will take precedence over any other layer
     */
    public static final OrderedKey TEST                  = OrderedKey.of("test",        100);
    
    /**
     * Layer reserved for code override and is normally attached to a settable config
     */
    public static final OrderedKey OVERRIDE              = OrderedKey.of("override",    200);

    /**
     * Layer with immutable system properties (-D)
     */
    public static final OrderedKey SYSTEM                = OrderedKey.of("sys",         300);
    
    /**
     * Layer with immutable environment properties
     */
    public static final OrderedKey ENVIRONMENT           = OrderedKey.of("env",         400);
    
    /**
     * Layer into which default environment properties may be loaded.  Legacy code include
     *
     */
    public static final OrderedKey ENVIRONMENT_DEFAULTS = OrderedKey.of("default",     500);

    /**
     * Layer reserved for remove configuration overrides from persistent storage
     */
    public static final OrderedKey REMOTE_OVERRIDE       = OrderedKey.of("remote",      600);
    
    /**
     * Override for application configuration loaded in property files.
     */
    public static final OrderedKey APPLICATION_OVERRIDE  = OrderedKey.of("app_override",700);

    /**
     * Layer to be used by the application for application specific configurations
     * and allows for the application to override any configuration loaded by libraries
     */
    public static final OrderedKey APPLICATION           = OrderedKey.of("app",         800);
    
    /**
     * Layer into which any class or 'library' may load its configuration
     */
    public static final OrderedKey LIBRARIES             = OrderedKey.of("libraries",   900);
}
