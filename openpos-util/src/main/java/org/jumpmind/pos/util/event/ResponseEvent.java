package org.jumpmind.pos.util.event;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseEvent<T> extends AppEvent {

    String targetDeviceId;
    String targetAppId;
    String request;
    ResponseEventStatusCode statusCode = ResponseEventStatusCode.OK;
    T payload;

    @Builder
    public ResponseEvent(String deviceId, String appId, String pairedDeviceId, boolean remote, String targetDeviceId, String targetAppId, String request, T payload, ResponseEventStatusCode statusCode) {
        super(deviceId, appId, pairedDeviceId, remote);
        this.targetDeviceId = targetDeviceId;
        this.targetAppId = targetAppId;
        this.request = request;
        this.payload = payload;
        this.statusCode = statusCode;
    }
}
