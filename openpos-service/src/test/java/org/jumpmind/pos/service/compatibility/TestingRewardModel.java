package org.jumpmind.pos.service.compatibility;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jumpmind.pos.persist.ColumnDef;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(exclude = "rewardAmount")
public class TestingRewardModel {

    String rewardId;
    BigDecimal rewardAmount;

}
