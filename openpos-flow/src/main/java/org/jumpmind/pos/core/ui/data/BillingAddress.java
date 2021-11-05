package org.jumpmind.pos.core.ui.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String firstName;
    private String lastName;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;

    @JsonIgnore
    public String getNameOnCard() {
        return String.format("%s %s", firstName, lastName);
    }
}