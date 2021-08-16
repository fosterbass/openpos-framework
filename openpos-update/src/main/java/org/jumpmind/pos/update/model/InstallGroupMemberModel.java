package org.jumpmind.pos.update.model;

import lombok.Data;
import org.jumpmind.pos.persist.AbstractModel;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

@Data
@TableDef(name="install_group_member", primaryKey = {"installationId"})
public class InstallGroupMemberModel extends AbstractModel {

    @ColumnDef
    String installationId;

    @ColumnDef
    String groupId;

}
