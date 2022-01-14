package org.jumpmind.pos.util.model;

import java.util.Map;

public interface IDeviceAttributes {
    String getDeviceId();
    String getAppId();
    String getLocale();
    String getTimezoneOffset();
    String getBusinessUnitId();
    String getDescription();
    String getDeviceMode();
    Map<String,String> getDeviceParamsMap();
}