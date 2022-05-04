package org.jumpmind.pos.devices.service.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetChildDevicesRequest {
    private String parentDeviceId;
    private String appId;
}
