package org.jumpmind.pos.devices.service.model;

import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PersonalizeMeRequest {
    private String deviceName;
    private Map<String, String> additionalAttributes;
}
