package org.jumpmind.pos.persist.model;

import lombok.Getter;
import lombok.Setter;
import org.jumpmind.pos.persist.ColumnDef;

import java.io.Serializable;

@Getter
@Setter
public class EffectiveAndEndVersion implements Serializable {

    @ColumnDef(description = "Software Version at which this data should become available", defaultValue = "0")
    int effectiveVersion;  // NOSONAR

    @ColumnDef(description = "Software Version at which any version after this data will expire and not become")
    int effectiveEndVersion;  // NOSONAR
}
