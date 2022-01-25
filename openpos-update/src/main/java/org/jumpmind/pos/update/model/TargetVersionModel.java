package org.jumpmind.pos.update.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jumpmind.pos.persist.AbstractModel;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableDef(name = "target_version", primaryKey = { "groupId", "effectiveTime" })
public class TargetVersionModel extends AbstractModel {
    @ColumnDef
    String groupId;

    @ColumnDef
    Date effectiveTime;

    @ColumnDef
    String version;
}
