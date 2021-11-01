package org.jumpmind.pos.util.event;

import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString(callSuper = true)
public class DeviceDisplayModeToggledEvent extends AppEvent {
    private static final long serialVersionUID = 1L;

    public DeviceDisplayModeToggledEvent(String deviceId, String appId, String pairedDeviceId) {
        super(deviceId, appId, pairedDeviceId);
    }
}
