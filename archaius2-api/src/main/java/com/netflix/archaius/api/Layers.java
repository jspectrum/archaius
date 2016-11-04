package com.netflix.archaius.api;

public final class Layers {
    public static final String OVERRIDE_LAYER    = "override";
    public static final String SYS_LAYER         = "sys";
    public static final String ENV_LAYER         = "env";
    public static final String APPLICATION_LAYER = "app";
    public static final String LIBRARIES_LAYER   = "app";
    public static final String REMOTE_LAYER      = "remote";
    public static final String DEFAULT_LAYER     = "default";

    public static final int OVERRIDE_LAYER_ORDER    = 160;
    public static final int SYS_LAYER_ORDER         = 150;
    public static final int ENV_LAYER_ORDER         = 140;
    public static final int APPLICATION_LAYER_ORDER = 130;
    public static final int LIBRARIES_LAYER_ORDER   = 120;
    public static final int REMOTE_LAYER_ORDER      = 110;
    public static final int DEFAULT_LAYER_ORDER     = 100;

}


