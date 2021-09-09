package org.jumpmind.pos.devices.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerLocation {
    private String address;
    private String port;
}
