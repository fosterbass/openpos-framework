package org.jumpmind.pos.core.ui.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class UIAddress implements Serializable {
    private static final long serialVersionUID = 1L;

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String type;
}
