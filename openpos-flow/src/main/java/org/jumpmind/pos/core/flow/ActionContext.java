package org.jumpmind.pos.core.flow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.server.model.Action;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionContext {

    Action action;
    StackTraceElement[] stackTrace;
    String syncId;

    public ActionContext(Action action) {
        this.action = action;
    }

    public ActionContext(Action action, StackTraceElement[] stackTrace) {
        this.action = action;
        this.stackTrace = stackTrace;
    }

    public void parseSyncId() {
        ActionSyncId actionSyncId = new ActionSyncId(action.getName());
        action.setName(actionSyncId.getActionName());
        this.syncId = actionSyncId.getSyncId();
    }
}
