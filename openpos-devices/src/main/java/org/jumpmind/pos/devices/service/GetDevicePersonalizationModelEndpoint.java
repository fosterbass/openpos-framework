package org.jumpmind.pos.devices.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jumpmind.pos.devices.model.DevicePersonalizationModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.PersonalizeMeResponse;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.util.JSONUtils;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Slf4j
@Endpoint(path = REST_API_CONTEXT_PATH + "/admin/personalizeMe")
public class GetDevicePersonalizationModelEndpoint {
    @Autowired
    private DevicesRepository repository;

    @Autowired
    JSONUtils jsonUtils;

    public PersonalizeMeResponse personalizeMe(@RequestParam("deviceName") String deviceName){
        log.info("Received auto-personalization request from device \"" + deviceName + "\"");
        DevicePersonalizationModel model = repository.findDevicePersonalizationModel(deviceName);
        if(model != null) {
            PersonalizeMeResponse response = buildResponse(model);
            return response;
        }
        return null;
    }

    protected PersonalizeMeResponse buildResponse(DevicePersonalizationModel model) {
        PersonalizeMeResponse response = new PersonalizeMeResponse(model);
        Map<String, String> personalizationParams = new HashMap<>();
        if (StringUtils.isNotBlank(response.getPersonalizationParamsString())) {
            personalizationParams = jsonUtils.toMap(response.getPersonalizationParamsString());
        }

        if (StringUtils.isNotBlank(model.getBusinessUnitId())) {
            personalizationParams.put(ClientContext.BUSINESS_UNIT_ID, model.getBusinessUnitId());
        }

        response.setPersonalizationParams(personalizationParams);
        return response;
    }
}

