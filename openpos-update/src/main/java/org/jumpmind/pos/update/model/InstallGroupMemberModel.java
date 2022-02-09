package org.jumpmind.pos.update.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jumpmind.pos.persist.AbstractModel;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableDef(name = "install_group_member", primaryKey = { "installationId" })
public class InstallGroupMemberModel extends AbstractModel {

    @ColumnDef
    String installationId;

    @ColumnDef
    String groupId;

    @ColumnDef
    Date lastUpdateCheck;
}
