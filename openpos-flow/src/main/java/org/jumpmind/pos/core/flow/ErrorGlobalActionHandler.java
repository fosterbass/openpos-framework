package org.jumpmind.pos.core.flow;

import org.jumpmind.pos.server.model.Action;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ErrorGlobalActionHandler {

    public static final String RESET_STATE_MANAGER = "ResetStateManager";

    @In(scope = ScopeType.Device)
    IStateManager stateManager;

    @Autowired
    IStateManagerContainer stateManagerContainer;

    @OnGlobalAction
    public void onResetStateManager(Action action) {
        stateManager.reset();

        final List<Device> children = stateManager.getChildDevices();
        if (children != null) {
            for (final Device child: children) {
                stateManagerContainer.resetStateManager(child.getDeviceId());
            }
        }
    }
}
