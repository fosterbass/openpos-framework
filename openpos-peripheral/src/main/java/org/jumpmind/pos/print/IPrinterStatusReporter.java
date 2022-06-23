package org.jumpmind.pos.print;

import org.jumpmind.pos.util.status.Status;

public interface IPrinterStatusReporter {
    void reportStatus(String printerId, Status status, String message);
}
