package org.jumpmind.pos.core.ui;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Refer to MatDialogConfig properties defined <a href="https://material.angular.io/components/dialog/api#MatDialogConfig">here</a>
 * for list of properties that can be supported as needed.  Also includes OpenPOS specific properties
 */
@Getter
@Setter
@NoArgsConstructor
public class DialogProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Use this to force a new dialog to be create if dialogs are being shown back to back.  The default behavior
     * is to reuse the previous dialog and just swap out the content.
     */
    private String minWidth;
    private String minHeight;
    private String maxWidth;
    private boolean executeActionBeforeClose = false;
    private boolean closeable = false;
    private boolean autoFocus = true;
    private boolean restoreFocus = true;

    public DialogProperties(boolean closeable) {
        this.closeable = closeable;
    }

    public DialogProperties executeActionBeforeClose(boolean execBeforeClose) {
        this.setExecuteActionBeforeClose(execBeforeClose);
        return this;
    }

    public DialogProperties closeable(boolean closeable) {
        this.setCloseable(closeable);
        return this;
    }

    public DialogProperties autoFocus(boolean autoFocus) {
        this.setAutoFocus(autoFocus);
        return this;
    }

    public DialogProperties minWidth(String minWidth) {
        this.setMinWidth(minWidth);
        return this;
    }

    public DialogProperties minHeight(String minHeight) {
        this.setMinHeight(minHeight);
        return this;
    }
}
