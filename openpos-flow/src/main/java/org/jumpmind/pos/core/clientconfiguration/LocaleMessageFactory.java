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

    @Value("${openpos.general.businessRegion:US}")
    String businessRegion;

    @Value("${openpos.general.i18nEnabled:false}")
    boolean i18nEnabled;

    public LocaleChangedMessage getMessage(Locale locale, Locale displayLocale) {
        LocaleChangedMessage message = new LocaleChangedMessage(locale, displayLocale);
        message.setSupportedLocales(supportedLocales);
        message.setShowIcons(showIcons);
        return message;
    }

    public LocaleChangedMessage getMessage() {
        LocaleChangedMessage message = new LocaleChangedMessage();
        if(i18nEnabled) {
            message.setRegion(businessRegion);
            message.setLocale(businessUnitLocale);
            message.setDisplayLocale(businessUnitLocale);
        } else {
            message.setRegion("US");
            message.setLocale("en_US");
            message.setDisplayLocale("en_US");
        }
        message.setSupportedLocales(supportedLocales);
        message.setShowIcons(showIcons);
        return message;
    }
}
