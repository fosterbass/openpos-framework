package org.jumpmind.pos.service.compatibility;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.jumpmind.pos.service.PosServerException;
import org.jumpmind.pos.service.filter.EndpointFilter;
import org.jumpmind.pos.service.filter.EndpointRequestFilter;
import org.jumpmind.pos.service.filter.EndpointResponseFilter;
import org.jumpmind.pos.service.filter.RequestContext;
import org.jumpmind.pos.util.MoneyCalculator;
import org.springframework.stereotype.Component;

@Component
@EndpointFilter
public class TestingEndpointFilters {

    @EndpointResponseFilter(path="/testingCustomer/getLoyaltyPromotions")
    public TestingRewardModelV1 filterNestedRewardModel(TestingRewardModel rewardModel) throws Exception {
        TestingRewardModelV1 rewardModelV1 = new TestingRewardModelV1();
        BeanUtils.copyProperties(rewardModelV1, rewardModel);
        rewardModelV1.setRewardAmount_V1(MoneyCalculator.money("USD", rewardModel.getRewardAmount()));
        return rewardModelV1;
    }

//    @EndpointResponseFilter(path="/testingCustomer/getLoyaltyPromotions")
//    public TestingRewardModel filterNestedRewardModel(TestingRewardModel rewardModel) throws Exception {
//        rewardModel.setRewardAmount(null);
//        return rewardModel;
//    }

    @EndpointResponseFilter(versionGreaterThan = "2.0", versionLessThan = "3.0")
    public TestingCustomerAccount filterNestedCustomerAccount(TestingCustomerAccount customerAccount) throws Exception {
        TestingCustomerAccountV1 customerAccountV1 = new TestingCustomerAccountV1();
        BeanUtils.copyProperties(customerAccountV1, customerAccount);

        customerAccountV1.setRewardAmountV1(MoneyCalculator.moneyUsd(10.00));
        return customerAccountV1;
    }

    @EndpointResponseFilter(versionGreaterThan = "0.8.999", versionLessThan = "1.0")
    public TestingCustomerModel filterCustomerResponseReplaceModel(TestingCustomerModel customerModel) throws Exception {
        TestingCustomerModelV09 replacementModel = new TestingCustomerModelV09();
        BeanUtils.copyProperties(customerModel, replacementModel);
        replacementModel.setCustomerV09Field("V09 field set");
        return replacementModel;
    }

    @EndpointResponseFilter(versionGreaterThan = "1.0", versionLessThan = "2.0")
    public TestingCustomerModel filterCustomerResponse(TestingCustomerModel customerModel) {
        if (!CollectionUtils.isEmpty(customerModel.getCustomerAccounts())) {
            String loyaltyId = customerModel.getCustomerAccounts().get(0).getCustomerAccountId();
            customerModel.setCustomerAccounts(null);
            customerModel.setLoyaltyCustomerId(loyaltyId);
        }

        return customerModel;
    }

    @EndpointRequestFilter(versionGreaterThan = "0.8.1", versionLessThan = "2.0")
    public TestingSaveCustomerRequest filterCustomerRequest(RequestContext context,
                                                            TestingCustomerModel customerModel) {
        if (context == null) {
            throw new PosServerException("Test fail = context should not be null");
        }
        // old versions used to send the straight customer model instead of the SaveCustomerRequest;
        TestingSaveCustomerRequest request = new TestingSaveCustomerRequest();
        request.setCustomerModel(customerModel);
        return request;
    }

    @EndpointResponseFilter(versionLessThan = "0.9")
    public TestingCustomerModel filterTestRequestObjectReplacement(TestingCustomerModelV09 oldCustomerModel) throws Exception {
        TestingCustomerModel replacementModel = new TestingCustomerModel();
        if (oldCustomerModel.getCustomerV09Field() == null) {
            throw new RuntimeException("Old model should have V09 field set.");
        }
        BeanUtils.copyProperties(oldCustomerModel, replacementModel);
        return replacementModel;
    }

    @EndpointResponseFilter(path = "/testingCustomer/unlinkCustomer", versionGreaterThan = "1.0", versionLessThan = "2.0")
    public TestingSaveCustomerResponse filterResponseOnPath(TestingSaveCustomerResponse response) throws Exception {
        response.getCustomerModel().setCustomerId("visited unlinkCustomer response filter");
        return response;
    }

    @EndpointRequestFilter(path = "/testingCustomer/unlinkCustomer", versionLessThan = "1.0")
    public TestingSaveCustomerRequest filterResponseOnPath(TestingSaveCustomerRequest request) throws Exception {
        request.getCustomerModel().setCustomerId("visited unlinkCustomer request filter");
        return request;
    }

}