package org.jumpmind.pos.persist.model;

import lombok.Getter;
import lombok.Setter;
import org.jumpmind.pos.persist.ColumnDef;

import java.io.Serializable;

@Getter
@Setter
public class EffectiveVersion implements Serializable {

    @ColumnDef(description = "Software Version at which this data should become available", defaultValue = "0")
    int effectiveVersion;  // NOSONAR

}
