package org.jumpmind.pos.update.model;

import lombok.*;
import org.jumpmind.pos.persist.AbstractModel;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.MODULE)
@EqualsAndHashCode(callSuper = true)
@TableDef(name = "target_version", primaryKey = { "id", "groupId", "packageName" })
public class TargetVersionModel extends AbstractModel {
    @ColumnDef(autoIncrement = true)
    Integer id;

    @ColumnDef
    String groupId;

    @ColumnDef
    String packageName;

    @ColumnDef
    Date effectiveTime;

    @ColumnDef
    String version;
}
