package org.jumpmind.pos.util.event;

import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString(callSuper = true)
public class DeviceUnpairedEvent extends AppEvent {
    private static final long serialVersionUID = 1L;

    public DeviceUnpairedEvent(String deviceId, String appId, String pairedDeviceId) {
        super(deviceId, appId, pairedDeviceId);
    }
}
