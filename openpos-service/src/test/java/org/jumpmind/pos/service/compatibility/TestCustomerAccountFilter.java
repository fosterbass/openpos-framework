package org.jumpmind.pos.service.compatibility;

import org.apache.commons.beanutils.BeanUtils;
import org.joda.money.Money;
import org.jumpmind.pos.service.filter.EndpointFilter;
import org.jumpmind.pos.service.filter.EndpointResponseFilter;
import org.jumpmind.pos.util.MoneyCalculator;
import org.springframework.stereotype.Component;

@Component
@EndpointFilter
public class TestCustomerAccountFilter {

    @EndpointResponseFilter(versionGreaterThan = "2.0", versionLessThan = "3.0")
    public TestingCustomerAccountV1 filterNestedCustomerAccount(TestingCustomerAccount customerAccount) throws Exception {
        TestingCustomerAccountV1 customerAccountV1 = new TestingCustomerAccountV1();
        BeanUtils.copyProperties(customerAccountV1, customerAccount);

        customerAccountV1.setRewardAmountV1(MoneyCalculator.moneyUsd(10.00));
        return customerAccountV1;
    }
}
