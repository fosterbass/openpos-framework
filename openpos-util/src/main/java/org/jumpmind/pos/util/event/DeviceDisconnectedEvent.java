package org.jumpmind.pos.util.event;

import lombok.ToString;

@ToString(callSuper = true)
public class DeviceDisconnectedEvent extends AppEvent {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private DeviceDisconnectedEvent() {}
    
    public DeviceDisconnectedEvent(String deviceId, String appId) {
        super(deviceId, appId);
    }
}
