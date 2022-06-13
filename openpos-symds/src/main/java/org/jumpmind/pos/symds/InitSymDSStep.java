package org.jumpmind.pos.symds;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.core.flow.*;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.message.DialogUIMessage;
import org.jumpmind.pos.core.ui.messagepart.DialogHeaderPart;
import org.jumpmind.pos.core.ui.messagepart.MessagePartConstants;
import org.jumpmind.pos.server.model.Action;
import org.jumpmind.pos.util.AppUtils;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.IRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class InitSymDSStep implements ITransitionStep {

    // todo: we can probably get rid of this entire state since it can't be reliably be enforced; client will do it.

    @Autowired(required = false)
    protected SymDSInitStatusProvider initStatusProvider;

    @Autowired(required = false)
    ISymmetricEngine symmetricEngine;

    @In(scope = ScopeType.Device)
    IStateManager stateManager;

    @Autowired
    private AsyncExecutor asyncExecutor;

    Transition transition;

    @InOut(scope = ScopeType.Device)
    boolean completed = false;

    @Override
    public boolean isApplicable(Transition transition) {
        this.transition = transition;
        return !completed;
    }

    @ActionHandler
    boolean onCheckAgain() {
        if (initStatusProvider != null && initStatusProvider.getCurrentStatus().isNotReady()) {
            showStatus();
            return true;
        } else {
            completed = true;
            transition.proceed();
            return false;
        }
    }

    @ActionHandler
    protected void onAnyAction(Action action) {
        log.info("Received an {} action that will be ignored", action.getName());
    }

    @Override
    public void arrive(Transition transition) {
        if (onCheckAgain()) {
            asyncExecutor.execute(null, req -> {
                do {
                    AppUtils.sleep(5000);
                    stateManager.doAction("CheckAgain");
                } while (initStatusProvider.getCurrentStatus().isNotReady());
                return null;
            }, res -> {
            }, err -> {
            });
        }
    }

    protected void showStatus() {
        IRegistrationService registrationService = symmetricEngine.getRegistrationService();
        INodeService nodeService = symmetricEngine.getNodeService();
        if (!registrationService.isRegisteredWithServer()) {
            showNotRegisteredUnit();
        } else if (!nodeService.isDataLoadCompleted()) {
            showDataLoadInProgress();
        }
    }

    protected void showNotRegisteredUnit() {
        DialogUIMessage screen = new DialogUIMessage();
        screen.setId("NotRegisteredDialog");
        DialogHeaderPart headerPart = new DialogHeaderPart();
        headerPart.setHeaderText("key:device:notregistered.header.label");
        screen.addMessagePart(MessagePartConstants.DialogHeader, headerPart);
        screen.addButton(new ActionItem("CheckAgain", "key:device:action.checkagain.label"));
        stateManager.showScreen(screen);
    }

    protected void showDataLoadInProgress() {
        DialogUIMessage screen = new DialogUIMessage();
        screen.setId("DataLoadInProgressDialog");
        DialogHeaderPart headerPart = new DialogHeaderPart();
        headerPart.setHeaderText("key:device:dataloadinprogress.header.label");
        screen.addMessagePart(MessagePartConstants.DialogHeader, headerPart);
        screen.addButton(new ActionItem("CheckAgain", "key:device:action.checkagain.label"));
        stateManager.showScreen(screen);
    }

}
