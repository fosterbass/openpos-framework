package org.jumpmind.pos.core.flow;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.server.model.Action;
import org.springframework.beans.factory.annotation.Autowired;

public class ErrorGlobalActionHandler {

    public static final String RESET_STATE_MANAGER = "ResetStateManager";

    @In(scope = ScopeType.Device)
    IStateManager stateManager;

    @Autowired
    IStateManagerContainer stateManagerContainer;

    @OnGlobalAction
    public void onResetStateManager(Action action) {
        stateManager.reset();
        if (StringUtils.isNotBlank(stateManager.getPairedDeviceId())) {
            stateManagerContainer.resetStateManager(stateManager.getPairedDeviceId());
        }
    }
}
