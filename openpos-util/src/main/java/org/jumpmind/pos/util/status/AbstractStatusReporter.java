package org.jumpmind.pos.util.status;

import ch.qos.logback.core.status.StatusManager;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractStatusReporter implements IStatusReporter {

    @Autowired
    protected ClientContext clientContext;

    private Map<String,IStatusManager> statusManagerByDeviceId = new ConcurrentHashMap<>();

    private Map<String, StatusReport> lastStatusById = new ConcurrentHashMap<>();

    @Override
    public StatusReport getStatus(IStatusManager statusManager, String deviceId) {
        this.statusManagerByDeviceId.put(deviceId, statusManager);
        StatusReport lastStatus = null;
        String id = getIdForLastStatus();
        if (id != null) {
            lastStatus = this.lastStatusById.get(id);
        }
        if (lastStatus == null) {
            return getUnknownStatusReport();
        } else {
            return lastStatus;
        }
    }

    abstract protected String getIdForLastStatus();

    abstract protected StatusReport getUnknownStatusReport();

    protected void recordAndPublishStatus(StatusReport report) {
        String id = getIdForLastStatus();
        if (id != null) {
            this.lastStatusById.put(id, report);
            IStatusManager statusManager =
                    statusManagerByDeviceId.get(clientContext.get(ClientContext.DEVICE_ID));
            if (statusManager != null) {
                statusManager.reportStatus(report);
            }
        }
    }
}
