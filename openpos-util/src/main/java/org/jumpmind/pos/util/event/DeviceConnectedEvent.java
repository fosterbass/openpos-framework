package org.jumpmind.pos.util.event;

import lombok.Data;
import org.jumpmind.pos.util.Version;
import org.jumpmind.pos.util.Versions;
import org.jumpmind.pos.util.event.AppEvent;

import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
public class DeviceConnectedEvent extends AppEvent {

    private static final long serialVersionUID = 1L;

    List<Version> deviceVersions;

    @SuppressWarnings("unused")
    private DeviceConnectedEvent() {}
    
    public DeviceConnectedEvent(String deviceId, String appId, List<Version> deviceVersions) {
        super(deviceId, appId);
        this.deviceVersions = deviceVersions;
    }
}
