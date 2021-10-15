package org.jumpmind.pos.server.model;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jumpmind.pos.util.model.Message;

@Data()
@EqualsAndHashCode(callSuper = true)
public class ProxyMessage extends Message {

    private static final String MESSAGE_TYPE = "Proxy";

    private static final long serialVersionUID = 1L;

    private UUID messageId;

    private String proxyType;

    private String action;

    private String payload;

    private Map<String, Object> additionalFields;

    public ProxyMessage() {
        this.setType(MESSAGE_TYPE);
        this.messageId = UUID.randomUUID();
    }

    public ProxyMessage(String messageId, String proxyType, String action, String payload) {
        this.setType(MESSAGE_TYPE);
        if (messageId != null) {
            this.messageId = UUID.fromString(messageId);
        }
        this.proxyType = proxyType;
        this.action = action;
        this.payload = payload;
    }

    @JsonIgnore
    public void setAdditionalField(String fieldName, Object fieldValue) {
        additionalFields.put(fieldName, fieldValue);
    }

    @JsonIgnore
    public Object getAdditionalField(String fieldName) {
        return additionalFields.get(fieldName);
    }

}
