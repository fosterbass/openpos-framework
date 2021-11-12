package org.jumpmind.pos.devices.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.devices.DevicesException;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component("GetBusinessUnitIdFromConfigStrategy")
public class GetBusinessUnitIdFromConfigStrategy implements IDeviceBusinessUnitIdStrategy {
    @Value("${openpos.businessunitId:undefined}")
    private String businessUnitId;

    @Override
    public String getBusinessUnitId(DeviceModel deviceModel) {
        if ("undefined".equals(businessUnitId)) {
            throw new DevicesException("openpos.businessunitId is undefined");
        }
        return businessUnitId;
    }
}
