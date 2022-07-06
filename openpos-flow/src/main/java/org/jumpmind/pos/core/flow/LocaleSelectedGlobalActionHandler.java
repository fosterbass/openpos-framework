package org.jumpmind.pos.core.flow;

import org.apache.commons.lang3.LocaleUtils;
import org.jumpmind.pos.core.service.ClientLocaleService;
import org.jumpmind.pos.core.service.LocaleType;
import org.jumpmind.pos.server.model.Action;

import java.util.Locale;

public class LocaleSelectedGlobalActionHandler {

    @In(scope = ScopeType.Device)
    IStateManager stateManager;

    @In(scope = ScopeType.Device, autoCreate = true)
    ClientLocaleService clientLocaleService;

    @OnGlobalAction
    public void onLocaleSelected(Action action) {
        Locale locale = LocaleUtils.toLocale(action.getData());
        clientLocaleService.setLocale(LocaleType.DISPLAY, locale, true);
        stateManager.refreshScreen();
    }

}