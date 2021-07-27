package org.jumpmind.pos.service.compatibility;

import lombok.Data;

import java.util.List;

@Data
public class TestingCustomerModel {

    private String customerId;
    private String loyaltyCustomerId; // V1
    private List<TestingCustomerAccount> customerAccounts; // V2

}
