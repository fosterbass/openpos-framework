package org.jumpmind.pos.symds;

import org.jumpmind.pos.util.BoolUtils;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.IRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

public class SymClient {

    protected Environment env;

    ISymmetricEngine symmetricEngine;

    public boolean isInitialLoadFinished() {
        if (symmetricEngine != null) {
            INodeService nodeService = symmetricEngine.getNodeService();
            if (BoolUtils.toBoolean(env.getProperty("openpos.symmetric.start", "false")) &&
                    BoolUtils.toBoolean(env.getProperty("openpos.symmetric.waitForInitialLoad", "false")) &&
                    !nodeService.isRegistrationServer()) {
                IRegistrationService registrationService = symmetricEngine.getRegistrationService();
                return !(!registrationService.isRegisteredWithServer() || !nodeService.isDataLoadCompleted());
            }
        }
        return true;
    }
}
