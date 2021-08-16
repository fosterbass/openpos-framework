package org.jumpmind.pos.update.model;

import lombok.Data;
import org.jumpmind.pos.persist.AbstractModel;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

@Data
@TableDef(name="install_group", primaryKey = {"groupId"})
public class InstallGroupModel extends AbstractModel {

    @ColumnDef
    String groupId;

    @ColumnDef
    String groupName;

    @ColumnDef
    String targetVersion;

}
