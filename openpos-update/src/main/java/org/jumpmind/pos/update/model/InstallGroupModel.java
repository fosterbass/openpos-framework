package org.jumpmind.pos.update.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jumpmind.pos.persist.AbstractModel;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

@Data
@EqualsAndHashCode(callSuper = true)
@TableDef(name="install_group", primaryKey = {"groupId"})
public class InstallGroupModel extends AbstractModel {

    @ColumnDef
    String groupId;

    @ColumnDef
    String groupName;

    @ColumnDef
    String targetVersion;

}
