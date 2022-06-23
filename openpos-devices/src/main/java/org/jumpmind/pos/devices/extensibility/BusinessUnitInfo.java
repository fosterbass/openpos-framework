package org.jumpmind.pos.devices.extensibility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BusinessUnitInfo {
    private final String id;
    private final String name;
    private final String locationHint;
}
