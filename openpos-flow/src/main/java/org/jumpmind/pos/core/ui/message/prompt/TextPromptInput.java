package org.jumpmind.pos.core.ui.message.prompt;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextPromptInput extends BaseInputPrompt<String> {
    private TextPromptInputKind kind;
    private Integer minimumLength;
    private Integer maximumLength;
    private String pattern;
    private boolean allowBarcodeScanEntry;
    private String placeholder;
    private boolean readOnly;

    public TextPromptInput(String id, String label, boolean required) {
        this(id, label, null, TextPromptInputKind.Text, required,null, null, null, false, null, null,false);
    }

    @Builder
    TextPromptInput(
            String id,
            String label,
            String defaultValue,
            TextPromptInputKind kind,
            boolean required,
            Integer minimumLength,
            Integer maximumLength,
            String pattern,
            boolean allowBarcodeScanEntry,
            String hint,
            String placeholder,
            boolean readOnly
    ) {
        super(id,  PromptItemTypes.TEXT, label, defaultValue, required, hint);

        this.kind = kind;
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
        this.pattern = pattern;
        this.allowBarcodeScanEntry = allowBarcodeScanEntry;
        this.placeholder = placeholder;
        this.readOnly = readOnly;
    }
}
