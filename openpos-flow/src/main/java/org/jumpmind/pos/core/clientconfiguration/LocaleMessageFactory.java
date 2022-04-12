package org.jumpmind.pos.core.clientconfiguration;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocaleMessageFactory {

    @Value("${openpos.ui.language.supportedLocales:null}")
    String[] supportedLocales;

    @Value("${openpos.ui.language.showIcons:true}")
    boolean showIcons;

    @Value("${openpos.general.businessUnitLocale:en_US}")
    String businessUnitLocale;

    public LocaleChangedMessage getMessage(Locale locale, Locale displayLocale) {
        LocaleChangedMessage message = new LocaleChangedMessage(locale, displayLocale);
        message.setSupportedLocales(supportedLocales);
        message.setShowIcons(showIcons);
        return message;
    }

    public LocaleChangedMessage getMessage() {
        LocaleChangedMessage message = new LocaleChangedMessage();
        message.setLocale(businessUnitLocale);
        message.setDisplayLocale(businessUnitLocale);

        message.setSupportedLocales(supportedLocales);
        message.setShowIcons(showIcons);
        return message;
    }
}
