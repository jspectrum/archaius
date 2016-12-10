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
    public static final Layer TEST                  = Layer.of("test",        100);
    
    /**
     * Layer reserved for code override and is normally attached to a settable config
     */
    public static final Layer OVERRIDE              = Layer.of("override",    200);

    /**
     * Layer with immutable system properties (-D)
     */
    public static final Layer SYSTEM                = Layer.of("sys",         300);
    
    /**
     * Layer with immutable environment properties
     */
    public static final Layer ENVIRONMENT           = Layer.of("env",         400);
    
    /**
     * Layer into which default environment properties may be loaded.  Legacy code include
     *
     */
    public static final Layer ENVIRONMENT_DEFAULTS = Layer.of("default",     500);

    /**
     * Layer reserved for remove configuration overrides from persistent storage
     */
    public static final Layer REMOTE_OVERRIDE       = Layer.of("remote",      600);
    
    /**
     * Override for application configuration loaded in property files.
     */
    public static final Layer APPLICATION_OVERRIDE  = Layer.of("app_override",700);

    /**
     * Layer to be used by the application for application specific configurations
     * and allows for the application to override any configuration loaded by libraries
     */
    public static final Layer APPLICATION           = Layer.of("app",         800);
    
    /**
     * Layer into which any class or 'library' may load its configuration
     */
    public static final Layer LIBRARIES             = Layer.of("libraries",   900);
}
