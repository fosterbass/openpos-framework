package org.jumpmind.pos.util.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString(callSuper = true)
@Getter
public class DevicePairedEvent extends AppEvent {
    private static final long serialVersionUID = 1L;

    private String currentUsername;

    public DevicePairedEvent(String deviceId, String appId, String currentUsername) {
        super(deviceId, appId);
        this.currentUsername = currentUsername;
    }
}
