package org.jumpmind.pos.devices.service.model;

import lombok.*;
import org.jumpmind.pos.devices.model.DeviceModel;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetChildDevicesResponse {
    @Builder.Default
    private List<DeviceModel> children = new ArrayList<>();
}
