package org.jumpmind.pos.service.compatibility;

import org.apache.commons.collections4.CollectionUtils;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Endpoint(path = "/testingCustomer/unlinkCustomer")
public class TestingUnlinkCustomerEndpoint {

    public TestingSaveCustomerResponse unlinkCustomer(@RequestBody TestingSaveCustomerRequest request) {
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