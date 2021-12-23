package org.jumpmind.pos.core.ui.message.prompt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseInputPrompt<T> extends PromptItem {
    private String label;
    private String hint;
    private T defaultValue;
    private boolean required;

    protected BaseInputPrompt(String id, String type, String label) {
        super(id, type);
        this.label = label;
    }

    protected BaseInputPrompt(String id, String type, String label, T defaultValue, boolean required, String hint) {
        super(id, type);
        this.label = label;
        this.hint = hint;
        this.defaultValue = defaultValue;
        this.required = required;
    }
}
