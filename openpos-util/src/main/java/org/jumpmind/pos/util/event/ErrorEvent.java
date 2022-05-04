package org.jumpmind.pos.util.event;

import lombok.Data;
import lombok.ToString;

@ToString(callSuper = true)
@Data
public class ErrorEvent extends AppEvent {
    private static final long serialVersionUID = 1L;

    private Throwable throwable;
    private String errorMessage;

    public ErrorEvent() {
        super();
    }

    public ErrorEvent(Throwable throwable) {
        super();
        this.throwable = throwable;
    }

    public ErrorEvent(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public ErrorEvent(String deviceId, String appId, String errorMessage) {
        super(deviceId, appId);
        this.errorMessage = errorMessage;
    }

    public ErrorEvent(String deviceId, String appId, Throwable throwable) {
        super(deviceId, appId);
        this.throwable = throwable;
    }
}
