package org.jumpmind.pos.core.flow;

import org.jumpmind.pos.core.screeninterceptor.IMessagePropertyStrategy;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ActionSyncMessagePropertyStrategy implements IMessagePropertyStrategy<UIMessage> {

    @Autowired
    StateManagerContainer stateManagerContainer;

    public static final String GLOBAL_SYNC_ID = "Global";

    @Override
    public Object doStrategy(String deviceId, Object property, Class<?> clazz, UIMessage message, Map<String, Object> messageContext) {
        if (property instanceof ActionItem) {
            ActionItem actionItem = (ActionItem) property;
            String syncId = getSyncId(actionItem);
            if (syncId != null && !actionItem.getAction().contains(ActionSyncId.SYNC_ID_DIVIDER)) {
                actionItem.setAction(new StringBuilder().append(actionItem.getAction()).
                        append(ActionSyncId.SYNC_ID_DIVIDER).append(syncId).toString());
            }
        }
        return property;
    }

    private String getSyncId(ActionItem actionItem) {
        if (actionItem.isGlobalActionFlag()) {
            return GLOBAL_SYNC_ID;
        } else {
            IStateManager stateManager = stateManagerContainer.getCurrentStateManager();
            if (stateManager != null && stateManager.getCurrentState() != null) {
                return stateManager.getCurrentState().getClass().getSimpleName();
            }
        }
        return null;
    }

}
