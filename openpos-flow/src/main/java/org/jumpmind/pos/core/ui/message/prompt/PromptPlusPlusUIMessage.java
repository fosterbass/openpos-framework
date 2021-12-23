package org.jumpmind.pos.core.ui.message.prompt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.message.UIMessageType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PromptPlusPlusUIMessage extends UIMessage {
    private List<PromptItem> items = new ArrayList<>();
    private ActionItem submitAction;

    private boolean submitOnScan;
    private String submitOnScanAction;

    public PromptPlusPlusUIMessage() {
        setScreenType(UIMessageType.PROMPT_PLUS_PLUS);
    }

    @Builder
    public PromptPlusPlusUIMessage(
        List<PromptItem> items,
        ActionItem submitAction,
        boolean submitOnScan,
        String submitOnScanAction
    ) {
       this();

       this.items = items;
       this.submitAction = submitAction;
       this.submitOnScan = submitOnScan;
       this.submitOnScanAction = submitOnScanAction;
    }
}
