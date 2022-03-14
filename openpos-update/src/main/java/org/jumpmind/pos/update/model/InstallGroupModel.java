package org.jumpmind.pos.update.model;

import lombok.*;
import org.jumpmind.pos.persist.AbstractModel;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@TableDef(name = "install_group", primaryKey = {"groupId"})
public class InstallGroupModel extends AbstractModel implements Serializable {
    @ColumnDef
    String groupId;

    @ColumnDef
    String groupName;

    private Map<String, String> packageVersions;
}
