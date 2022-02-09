package org.jumpmind.pos.devices.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.DevicesException;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DeviceParamModel;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component("GetBusinessUnitIdFromPersonalizationParamsStrategy")
public class GetBusinessUnitIdFromPersonalizationParamsStrategy implements IDeviceBusinessUnitIdStrategy {
    @Override
    public String getBusinessUnitId(DeviceModel deviceModel) {
        if (CollectionUtils.isNotEmpty(deviceModel.getDeviceParamModels())) {
            String paramBusinessUnitId = deviceModel.getDeviceParamModels().stream().filter(dm -> ClientContext.BUSINESS_UNIT_ID.equals(dm.getParamName())).findFirst().map(DeviceParamModel::getParamValue).orElse(null);
            if (StringUtils.isNotBlank(paramBusinessUnitId)) {
                log.info("BusinessUnitId '{}' read via personalization parameters for deviceId: {}, appId: {}",
                        paramBusinessUnitId, deviceModel.getDeviceId(), deviceModel.getAppId());
                return paramBusinessUnitId;
            } else {
                throw new DevicesException(String.format("BusinessUnitId is EMPTY in personalization parameters for deviceId: %s, appId: %s", deviceModel.getDeviceId(), deviceModel.getAppId()));
            }
        } else {
            throw new DevicesException(String.format("No personalization parameters from which to read businessUnitId for deviceId: %s, appId: %s",
                deviceModel.getDeviceId(), deviceModel.getAppId()));
        }
    }
}
