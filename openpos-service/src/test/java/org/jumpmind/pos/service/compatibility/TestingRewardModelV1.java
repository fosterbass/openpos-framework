package org.jumpmind.pos.service.compatibility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.joda.money.Money;

@Data
public class TestingRewardModelV1 extends TestingRewardModel {

    @JsonProperty(value = "rewardAmount", access = JsonProperty.Access.READ_ONLY)
    Money rewardAmount_V1;
}
