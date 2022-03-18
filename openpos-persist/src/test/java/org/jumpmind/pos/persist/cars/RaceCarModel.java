package org.jumpmind.pos.persist.cars;

import lombok.Data;
import org.joda.money.Money;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

@TableDef(name="race_car",
        primaryKey = "vin")
@Data
public class RaceCarModel extends CarModel {

    private static final long serialVersionUID = 1L;
    
    @ColumnDef
    private boolean turboCharged;

    @ColumnDef(crossReference="isoCurrencyCode")
    private Money lastSoldAmount;

}
