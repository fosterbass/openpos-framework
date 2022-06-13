package org.jumpmind.pos.symds;

import org.jumpmind.pos.service.init.IModuleStatusProvider;
import org.jumpmind.pos.service.init.ModuleInitStatus;
import org.jumpmind.pos.util.BoolUtils;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.IRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component(SymDSModule.NAME + "InitStatusProvider")
public class SymDSInitStatusProvider implements IModuleStatusProvider {
    @Autowired
    private Environment env;

    @Autowired(required = false)
    private ISymmetricEngine symmetricEngine;

    @Override
    public String getDisplayName() {
        return "Initial Data Load";
    }

    @Override
    public ModuleInitStatus getCurrentStatus() {
        if (symmetricEngine == null) {
            return ModuleInitStatus.ready();
        }

        INodeService nodeService = symmetricEngine.getNodeService();

        boolean waitForInitEnabled =
                BoolUtils.toBoolean(env.getProperty("openpos.symmetric.start", "false"))
                && BoolUtils.toBoolean(env.getProperty("openpos.symmetric.waitForInitialLoad", "false"))
                && !nodeService.isRegistrationServer();

        if (waitForInitEnabled) {
            IRegistrationService registrationService = symmetricEngine.getRegistrationService();
            boolean isInitialized = registrationService.isRegisteredWithServer() && nodeService.isDataLoadCompleted();

            if (!isInitialized) {
                return ModuleInitStatus.notReady("Loading initial data...");
            }
        }

        return ModuleInitStatus.ready();
    }
}
