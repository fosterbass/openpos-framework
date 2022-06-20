package org.jumpmind.pos.util.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public abstract class AppEvent extends Event implements Serializable {
    private static final long serialVersionUID = 1L;

    String deviceId;
    String appId;
    boolean remote;

    public AppEvent() {
        super();
    }

    public AppEvent(String deviceId, String appId) {
        this(deviceId, appId, false);
    }

    public AppEvent(String deviceId, String appId, boolean remote) {
        super(createSourceString(appId, deviceId));
        this.deviceId = deviceId;
        this.appId = appId;
        this.remote = remote;
    }

    public static String createSourceString(String appId, String deviceId) {
        return appId + "/" + deviceId;
    }

    @Override
    public String getSource() {
        return createSourceString(appId, deviceId);
    }
}
