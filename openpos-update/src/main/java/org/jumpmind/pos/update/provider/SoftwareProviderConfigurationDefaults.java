package org.jumpmind.pos.update.provider;

public class SoftwareProviderConfigurationDefaults {
    public static final String FILE_NAME_PATTERN = "^(\\w+-)+(?<version>(\\d+)(\\.(\\d+)(\\.(\\d+))?)?(-+([\\w-]+[\\w\\-.]+))?(\\+([\\w-]+[\\w\\-.]+))?)\\.zip$";
    public static final boolean FILE_NAME_PATTERN_IGNORE_CASE = false;

    private SoftwareProviderConfigurationDefaults() {}
}
