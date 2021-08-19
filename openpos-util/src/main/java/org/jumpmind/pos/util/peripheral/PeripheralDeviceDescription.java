package org.jumpmind.pos.util.peripheral;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PeripheralDeviceDescription {
    String id;
    String displayName;
    boolean requiresConfirmation;
    String confirmationTitle;
    String confirmationMessage;

    public PeripheralDeviceDescription(String id, String displayName) {
        this(id, displayName, false, null, null);
    }
}
