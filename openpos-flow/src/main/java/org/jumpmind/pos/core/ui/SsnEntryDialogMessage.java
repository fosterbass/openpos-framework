package org.jumpmind.pos.core.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


import static org.jumpmind.pos.core.ui.message.UIMessageType.SSN_ENTRY;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
public class SsnEntryDialogMessage extends UIMessage {

    private String enterCodeDescription;
    private String reenterCodeDescription;
    private String error;
    private ActionItem cancelAction;
    private ActionItem nextAction;

    public SsnEntryDialogMessage() {
        setScreenType(SSN_ENTRY);
        setId("ssn-entry");
    }
}
