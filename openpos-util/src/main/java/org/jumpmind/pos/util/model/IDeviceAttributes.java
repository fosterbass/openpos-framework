package org.jumpmind.pos.util.model;

import java.util.Map;

public interface IDeviceAttributes {
    String getDeviceId();
    String getAppId();
    String getTimezoneOffset();
    String getBusinessUnitId();
    String getDescription();
    Map<String,String> getDeviceParamsMap();
}