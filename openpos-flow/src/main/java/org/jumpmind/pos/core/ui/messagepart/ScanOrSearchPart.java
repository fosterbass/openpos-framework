package org.jumpmind.pos.core.ui.messagepart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.core.model.FieldInputType;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.IconType;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanOrSearchPart implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ScanType {
        CAMERA_CORDOVA, NONE
    }

    @Builder.Default
    private Integer scanMinLength = 1;
    @Builder.Default
    private Integer scanMaxLength = 22;
    private ScanType scanType;
    @Builder.Default
    private String scanIcon = IconType.Barcode;
    @Builder.Default
    private ActionItem scanAction = new ActionItem("Scan");
    @Builder.Default
    private ActionItem keyedAction = new ActionItem("HandKeyed");
    @Builder.Default
    private String scanSomethingText = "Scan/Key Something";
    private boolean autoFocusOnScan = false;
    @Builder.Default
    private FieldInputType inputType = FieldInputType.WordText;
    private String keyboardLayout;
}
