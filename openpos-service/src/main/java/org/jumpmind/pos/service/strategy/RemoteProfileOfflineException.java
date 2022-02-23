package org.jumpmind.pos.service.strategy;

import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;

public class RemoteProfileOfflineException extends ResourceAccessException {
    public RemoteProfileOfflineException(String msg) {
        super(msg);
    }

    public RemoteProfileOfflineException(String msg, IOException ex) {
        super(msg, ex);
    }

    public RemoteProfileOfflineException(ResourceAccessException ex) {
        super(ex != null ? ex.getMessage() : "");
        if (ex != null) {
            this.setStackTrace(ex.getStackTrace());
        }
    }

}
