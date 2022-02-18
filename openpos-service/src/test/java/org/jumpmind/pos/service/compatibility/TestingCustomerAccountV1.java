package org.jumpmind.pos.service.compatibility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.joda.money.Money;

import java.math.BigDecimal;

@Getter
@Setter
public class TestingCustomerAccountV1 extends TestingCustomerAccount {

    @JsonProperty(value = "rewardAmount", access = JsonProperty.Access.READ_ONLY)
    private Money rewardAmountV1;
}
