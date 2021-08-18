package org.jumpmind.pos.devices.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.devices.model.DevicePersonalizationModel;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonalizeMeResponse {
    private String deviceName;
    private String serverAddress;
    private String serverPort;
    private String deviceId;
    private String appId;
    private Map<String,String> personalizationParams;
    @JsonIgnore
    private String personalizationParamsString;

    public PersonalizeMeResponse(DevicePersonalizationModel model) {
        this.deviceName = model.getDeviceName();
        this.serverAddress = model.getServerAddress();
        this.serverPort = model.getServerPort();
        this.deviceId = model.getDeviceId();
        this.appId = model.getAppId();
        this.personalizationParamsString = model.getPersonalizationParams();
    }

    public PersonalizeMeResponse(String deviceName, String serverAddress, String serverPort, String deviceId, String appId, String personalizationParams) {
        this.deviceName = deviceName;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.deviceId = deviceId;
        this.appId = appId;
        this.personalizationParamsString = personalizationParams;
    }
}
