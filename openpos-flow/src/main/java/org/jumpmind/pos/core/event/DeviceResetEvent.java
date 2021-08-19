package org.jumpmind.pos.core.event;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jumpmind.pos.util.event.AppEvent;

@ToString(callSuper = true)
@NoArgsConstructor
public class DeviceResetEvent extends AppEvent {

    private static final long serialVersionUID = 1L;

    public DeviceResetEvent(String deviceId, String appId) {
        super(deviceId, appId);
    }
}
