package org.jumpmind.pos.translate;

import org.jumpmind.pos.core.model.Form;
import org.jumpmind.pos.core.model.POSSessionInfo;
import org.jumpmind.pos.server.model.Action;

public interface ITranslator {

    public void handleAction(ITranslationManagerSubscriber subscriber, TranslationManagerServer tmServer, Action action, Form formResults);
    
    public void setPosSessionInfo(POSSessionInfo posSessionInfo);

    /**
     * If you don't want to show a screen for this translator, you can override
     * this method to do other work (such as sending an action) and then return
     * {@code true} to indicate that you intercepted the translation. This can be
     * useful for situations within a given translator where under some circumstances
     * you want to do the screen translation and under other circumstances, you 
     * don't want to do screen translation.  For circumstances where you never
     * want to do screen translation, you can also use {@link IActionTranslator}.
     * @return {@code true} if you intercepted the translation process, 
     * {@code false} otherwise. Default is {@code false}.
     */
    default boolean intercept(TranslationManagerServer server) { return false; }
    
}
