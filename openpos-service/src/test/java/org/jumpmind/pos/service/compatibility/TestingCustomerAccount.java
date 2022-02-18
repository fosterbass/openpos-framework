package org.jumpmind.pos.service.compatibility;

import lombok.Data;
import org.joda.money.Money;

import java.math.BigDecimal;

@Data
public class TestingCustomerAccount {
    private String customerAccountId;
    private String accountType;
    private BigDecimal rewardAmount;

}