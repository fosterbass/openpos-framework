package org.jumpmind.pos.util.event;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestEvent<T> extends AppEvent {
    private String targetDeviceId;
    private String targetAppId;
    private String request;
    private String causedBy;
    private T payload;

    @Builder
    public RequestEvent(String deviceId, String appId, String pairedDeviceId, String targetDeviceId, String targetAppId, String request, String causedBy, T payload, boolean remote) {
        super(deviceId, appId, pairedDeviceId);
        this.targetDeviceId = targetDeviceId;
        this.targetAppId = targetAppId;
        this.request = request;
        this.causedBy = causedBy;
        this.payload = payload;
    }
}
