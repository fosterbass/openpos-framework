package org.jumpmind.pos.devices.service;


import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.devices.model.DevicePersonalizationModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.PersonalizeMeResponse;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestParam;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Slf4j
@Endpoint(path = REST_API_CONTEXT_PATH + "/admin/personalizeMe")
public class GetDevicePersonalizationModelEndpoint {
    @Autowired
    private DevicesRepository repository;

    public PersonalizeMeResponse personalizeMe(@RequestParam("deviceName") String deviceName){
        DevicePersonalizationModel model = repository.findDevicePersonalizationModel(deviceName);
        if(model != null) {
            PersonalizeMeResponse response = buildResponse(model);
            return response;
        }
        return null;
    }

    protected PersonalizeMeResponse buildResponse(DevicePersonalizationModel model) {
        PersonalizeMeResponse response = new PersonalizeMeResponse(model);
        if (StringUtils.isNotBlank(model.getBusinessUnitId()) && response.getPersonalizationParams() != null &&
            ! response.getPersonalizationParams().containsKey(ClientContext.BUSINESS_UNIT_ID)) {
            response.getPersonalizationParams().put(ClientContext.BUSINESS_UNIT_ID, model.getBusinessUnitId());
        }
        return response;
    }

}

