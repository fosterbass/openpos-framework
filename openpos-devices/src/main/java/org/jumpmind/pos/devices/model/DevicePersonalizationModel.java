package org.jumpmind.pos.devices.model;

import lombok.*;
import org.jumpmind.pos.persist.AbstractModel;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.TableDef;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@TableDef(name = "device_personalization", primaryKey = {"deviceName"})
@NoArgsConstructor
@AllArgsConstructor
public class DevicePersonalizationModel extends AbstractModel {
    @ColumnDef
    private String deviceName;
    @ColumnDef
    private String serverAddress;
    @ColumnDef
    private String serverPort;
    @ColumnDef
    private String deviceId;
    @ColumnDef
    private String appId;
    @ColumnDef
    private String businessUnitId;
    @ColumnDef
    private boolean sslEnabledFlag;

    private List<DeviceParamModel> deviceParamModels;
}
