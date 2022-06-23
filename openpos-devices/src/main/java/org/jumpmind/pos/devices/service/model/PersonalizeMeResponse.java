package org.jumpmind.pos.devices.service.model;

import lombok.*;
import org.jumpmind.pos.devices.model.DeviceParamModel;
import org.jumpmind.pos.devices.model.DevicePersonalizationModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalizeMeResponse {
    private String deviceToken;
    private String deviceName;
    private String serverAddress;
    private String serverPort;
    private String businessUnitId;
    private String deviceId;
    private String appId;
    private boolean sslEnabled = false;
    private String pairedAppId;
    private String pairedDeviceId;
    private List<ServerLocation> failoverAddresses;
    private Map<String, String> personalizationParams;

    public PersonalizeMeResponse(DevicePersonalizationModel model) {
        this.deviceName = model.getDeviceName();
        this.serverAddress = model.getServerAddress();
        this.serverPort = model.getServerPort();
        this.businessUnitId = model.getBusinessUnitId();
        this.deviceId = model.getDeviceId();
        this.appId = model.getAppId();
        this.sslEnabled = model.isSslEnabledFlag();
        this.personalizationParams = model.getDeviceParamModels()
            .stream()
            .collect(Collectors.toMap(DeviceParamModel::getParamName, DeviceParamModel::getParamValue));
    }
}
