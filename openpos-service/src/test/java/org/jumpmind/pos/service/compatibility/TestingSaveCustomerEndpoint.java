package org.jumpmind.pos.service.compatibility;

import org.apache.commons.collections4.CollectionUtils;
import org.jumpmind.pos.service.Endpoint;

import java.util.ArrayList;
import java.util.List;

@Endpoint(path="/testingCustomer/saveCustomer")
public class TestingSaveCustomerEndpoint {

    public TestingSaveCustomerResponse saveCustomer(TestingSaveCustomerRequest request) {
        if (request == null || request.getCustomerModel() == null || request.getCustomerModel().getCustomerId() == null) {
            throw new RuntimeException("Invalid request=" + request);
        }

        TestingSaveCustomerResponse response = new TestingSaveCustomerResponse();
        if (CollectionUtils.isEmpty(request.getCustomerModel().getCustomerAccounts())) {
            List<TestingCustomerAccount> accounts = new ArrayList<>();
            TestingCustomerAccount account = new TestingCustomerAccount();
            account.setAccountType("LOYALTY");
            account.setCustomerAccountId("5678");
            accounts.add(account);
            request.getCustomerModel().setCustomerAccounts(accounts);
        }
        response.setCustomerModel(request.getCustomerModel());
        return response;
    }


}
