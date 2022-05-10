package org.jumpmind.pos.core.service;

import java.util.Locale;

import org.jumpmind.pos.core.clientconfiguration.LocaleChangedMessage;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.core.flow.In;
import org.jumpmind.pos.core.flow.ScopeType;
import org.jumpmind.pos.server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("device")
public class ClientLocaleService {

    @In(scope = ScopeType.Device)
    IStateManager stateManager;

    @Autowired
    MessageService messageService;

    @Value("${openpos.ui.language.supportedLocales:null}")
    String[] supportedLocales;

    @Value("${openpos.ui.language.showIcons:true}")
    boolean showIcons;

    private Locale locale;

    private Locale displayLocale;

    public Locale getLocale(LocaleType localeType) {
        if (LocaleType.DISPLAY == localeType) {
            return this.displayLocale;
        } else {
            return this.locale;
        }
    }

    public void setLocale(LocaleType localeType, Locale locale, boolean updateClientLocale) {
        if (LocaleType.DISPLAY == localeType) {
            this.displayLocale = locale;
        } else {
            this.locale = locale;
        }
        if (updateClientLocale) {
            updateClientLocale();
        }
    }

    public void updateClientLocale() {
        messageService.sendMessage(stateManager.getDevice().getDeviceId(), getMessage(locale, displayLocale));
    }

    private LocaleChangedMessage getMessage(Locale locale, Locale displayLocale) {
        LocaleChangedMessage message = new LocaleChangedMessage(locale, displayLocale);
        message.setSupportedLocales(supportedLocales);
        message.setShowIcons(showIcons);
        return message;
    }

}
