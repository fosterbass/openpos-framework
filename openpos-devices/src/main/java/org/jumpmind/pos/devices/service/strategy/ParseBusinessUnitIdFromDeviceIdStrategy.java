package org.jumpmind.pos.devices.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.DeviceParser;
import org.jumpmind.pos.devices.DevicesException;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("ParseBusinessUnitIdFromDeviceIdStrategy")
public class ParseBusinessUnitIdFromDeviceIdStrategy implements IDeviceBusinessUnitIdStrategy {

    @Autowired
    DeviceParser deviceParser;

    @Override
    public String getBusinessUnitId(DeviceModel deviceModel) {

        String businessUnitId = deviceParser.getBusinessUnitId(deviceModel.getDeviceId());
        if (StringUtils.isBlank(businessUnitId)) {
            throw new DevicesException(String.format("Failed to extract businessUnitId from deviceId: %s", deviceModel.getDeviceId()));
        }

        return businessUnitId;
    }

}
