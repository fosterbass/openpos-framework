package org.jumpmind.pos.core.ui.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCustomer implements Serializable {
    private String iconName;
    private String fullName;
    private String phone;
    private String email;
    private Address address;
}
