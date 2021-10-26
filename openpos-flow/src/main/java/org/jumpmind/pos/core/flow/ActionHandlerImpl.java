package org.jumpmind.pos.core.flow;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.core.flow.config.FlowUtil;
import org.jumpmind.pos.server.model.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.jumpmind.pos.core.flow.ActionSyncMessagePropertyStrategy.GLOBAL_SYNC_ID;

// TODO should be called just ActionHandler, need to repackage annotation of the same name.
@Component
@AllArgsConstructor
@NoArgsConstructor
@org.springframework.context.annotation.Scope("prototype")
@Slf4j
public class ActionHandlerImpl {

    @Autowired
    private IBeforeActionService beforeActionService;

    @Autowired
    private ActionHandlerHelper helper;

    public boolean canHandleAction(Object state, ActionContext actionContext) {
        // if there is an action handler
        // AND it's not the current state firing this action.
        Method actionMethod = helper.getActionMethod(state, actionContext.getAction());
        if (actionMethod != null && !isCalledFromState(state, actionContext)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean handleAction(IStateManager stateManager, Object state, Action action) {
        // Get list of @BeforeAction methods in the state and execute them first
        beforeActionService.executeBeforeActionMethods(stateManager, state, action);

        Method actionMethod = helper.getActionMethod(state, action);
        if (actionMethod != null) {
            helper.invokeActionMethod(stateManager, state, action, actionMethod);
            return true;
        } else {
            return false;
        }
    }

    public boolean checkActionSync(IStateManager stateManager, ActionContext actionContext) {
        if (actionContext.getSyncId() == null
                || actionContext.getSyncId().equals(GLOBAL_SYNC_ID)
                || stateManager.getCurrentState() == null) {
            return true;
        }
        String currentStateName = stateManager.getCurrentState().getClass().getSimpleName();
        boolean syncOk = StringUtils.equals(currentStateName, actionContext.getSyncId());
        if (!syncOk) {
            log.warn("Discarding out of sync action: " + actionContext + " - current state is " +
                    currentStateName + " stateManager.getActionQueueSize()=" + stateManager.getActionQueueSize() );
            if (stateManager.getActionQueueSize() == 0) {
                // if there are no subsequently pending actions, refresh the current screen to help
                // avoid the client getting stuck on the "loading" screen.
                stateManager.refreshScreen();
            }
        }
        return syncOk;
    }

    public boolean canHandleAnyAction(Object state, ActionContext actionContext) {
        return helper.getAnyActionMethod(state) != null && !isCalledFromState(state, actionContext);
    }

    public boolean handleAnyAction(IStateManager stateManager, Object state, Action action) {
        // Get list of @BeforeAction methods in the state and execute them first
        beforeActionService.executeBeforeActionMethods(stateManager, state, action);

        Method anyActionMethod = helper.getAnyActionMethod(state);
        if (anyActionMethod != null) {
            helper.invokeActionMethod(stateManager, state, action, anyActionMethod);
            return true;
        } else {
            return false;
        }
    }


    protected boolean isCalledFromState(Object state, ActionContext actionContext) {
        StackTraceElement[] stackTrace = actionContext.getStackTrace();
        if (stackTrace == null) {
            return false;
        }

        if (stackTrace.length > 150) {
            helper.checkStackOverflow(StateManager.class, state, stackTrace);
        }

        for (StackTraceElement stackFrame : stackTrace) {
            Class<?> currentClass = helper.getClassFrom(stackFrame);
            if (currentClass != null) {
                if (!Modifier.isAbstract(currentClass.getModifiers()) && FlowUtil.isState(currentClass)
                        && !currentClass.isAssignableFrom(state.getClass())) {
                    return false;
                } else if (currentClass.isAssignableFrom(state.getClass())) {
                    return true;
                }
            }

        }

        return false;
    }


    public IBeforeActionService getBeforeActionService() {
        return beforeActionService;
    }

    public void setBeforeActionService(IBeforeActionService beforeActionService) {
        this.beforeActionService = beforeActionService;
    }

    public ActionHandlerHelper getActionHandlerHelper() {
        return helper;
    }

    public void setActionHandlerHelper(ActionHandlerHelper actionHandlerHelper) {
        this.helper = actionHandlerHelper;
    }
}
