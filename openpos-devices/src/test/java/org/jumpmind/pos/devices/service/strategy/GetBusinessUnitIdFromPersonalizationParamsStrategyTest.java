package org.jumpmind.pos.devices.service.strategy;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.DevicesException;
import org.jumpmind.pos.devices.TestDevicesConfig;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DeviceParamModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest()
@AutoConfigureMockMvc
@ContextConfiguration(classes = { TestDevicesConfig.class })
public class GetBusinessUnitIdFromPersonalizationParamsStrategyTest {
    @Autowired
    GetBusinessUnitIdFromPersonalizationParamsStrategy strategy;

    @Test
    public void validBusinessUnitIdParamTest() {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.setDeviceId("09999-001");
        List<DeviceParamModel> deviceParamModels = new ArrayList<>();
        deviceParamModels.add(new DeviceParamModel(deviceModel.getDeviceId(), "pos", "businessUnitId", "10000"));
        deviceModel.setDeviceParamModels(deviceParamModels);
        assertEquals("10000", strategy.getBusinessUnitId(deviceModel));
    }

    @Test(expected = DevicesException.class)
    public void businessUnitIdDoesNotExistInParamsTest() {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.setDeviceId("09999-001");
        strategy.getBusinessUnitId(deviceModel);
    }

}