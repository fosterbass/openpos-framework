package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.model.MessageType;
import org.jumpmind.pos.util.model.Message;

@Data
public class LoadingMessage extends Message {
    String title;
    boolean queue;
    boolean cancel;

    public LoadingMessage() {
        setType(MessageType.Loading);
        // Cannot set a label here because only UIMessages are
        // sent through the i18nScreenPropertyStrategy
        // this.title = "key:common:label.loading";
    }

    public LoadingMessage(String title) {
        this();
        this.title = title;
    }

    public LoadingMessage(String title, boolean queue, boolean cancel) {
        this(title);
        this.queue = queue;
        this.cancel = cancel;
    }
}
