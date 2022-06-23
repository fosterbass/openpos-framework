package org.jumpmind.pos.devices.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class PersonalizationConfigDevice {
    private final String businessUnitId;
    private final String deviceId;
    private final String appId;
    private final String authToken;
    private final Map<String, String> personalizationParamValues;
    private final Boolean connected;
}
