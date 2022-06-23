package org.jumpmind.pos.devices.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalizationParameter implements Serializable {
    private static final long serialVersionUID = 1L;

    private String property;
    private String label;
    private String defaultValue;
}