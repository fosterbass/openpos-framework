package org.jumpmind.pos.core.flow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class Device {
    private String appId;
    private String deviceId;

    @Override
    public String toString() {
        return String.format("%s:%s", deviceId, appId);
    }
}
