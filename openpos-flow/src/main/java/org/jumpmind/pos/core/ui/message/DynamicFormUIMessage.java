package org.jumpmind.pos.core.ui.message;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jumpmind.pos.core.model.Form;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.IHasForm;
import org.jumpmind.pos.core.ui.UIMessage;

@EqualsAndHashCode(callSuper = true)
@Data
public class DynamicFormUIMessage extends UIMessage implements IHasForm {

    private Form form = new Form();

    private ActionItem submitButton;

    private List<ActionItem> secondaryButtons = new ArrayList<>();

    private String instructions;

    private List<String> alternateSubmitActions = new ArrayList<>();

    private String imageUrl;

    public DynamicFormUIMessage() {
        setScreenType(UIMessageType.DYNAMIC_FORM);
    }

    public void addSecondaryButton(ActionItem actionItem) {
        secondaryButtons.add(actionItem);
    }

}
