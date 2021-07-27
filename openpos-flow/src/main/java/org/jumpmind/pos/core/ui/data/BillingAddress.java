package org.jumpmind.pos.core.ui.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingAddress implements Serializable {
    private String nameOnCard;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
}
