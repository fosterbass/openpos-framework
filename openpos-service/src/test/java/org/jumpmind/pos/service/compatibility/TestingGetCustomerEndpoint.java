package org.jumpmind.pos.service.compatibility;

import org.jumpmind.pos.service.Endpoint;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Endpoint(path = "/testingCustomer/getCustomer")
public class TestingGetCustomerEndpoint {

    public TestingGetCustomerResponse getCustomer(@RequestBody String customerId) {
        System.out.println("getCustomer called.");

        TestingGetCustomerResponse response = new TestingGetCustomerResponse();
        TestingCustomerModel customer = new TestingCustomerModel();
        customer.setCustomerId(customerId);

        List<TestingCustomerAccount> accounts = new ArrayList<>();
        {
            TestingCustomerAccount account = new TestingCustomerAccount();
            account.setCustomerAccountId("5678");
            account.setAccountType("LOYALTY");
            account.setRewardAmount(new BigDecimal("50.00"));
            accounts.add(account);
            accounts.add(account);
        }

        customer.setCustomerAccounts(accounts);

        response.setCustomerModel(customer);

        return response;
    }
}