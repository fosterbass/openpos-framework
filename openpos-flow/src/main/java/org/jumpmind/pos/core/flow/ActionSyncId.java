package org.jumpmind.pos.core.flow;

import lombok.Data;

@Data
public class ActionSyncId {

    public final static String SYNC_ID_DIVIDER = "@";

    String actionName;
    String syncId;

    public ActionSyncId(String action) {
        parseSyncId(action);
    }

    public void parseSyncId(String action) {
        if (action.contains(SYNC_ID_DIVIDER)) {
            int dividerIndex = action.indexOf(SYNC_ID_DIVIDER);
            actionName = action.substring(0, dividerIndex);
            syncId = action.substring(dividerIndex+1);
        } else {
            actionName = action;
        }
    }

}
