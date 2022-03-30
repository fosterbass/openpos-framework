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
@TableDef(name = "install_group_member", primaryKey = { "businessUnitId", "groupId" })
public class InstallGroupMemberModel extends AbstractModel {
    @ColumnDef
    String businessUnitId;

    @ColumnDef
    String groupId;
}
