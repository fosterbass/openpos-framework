package org.jumpmind.pos.devices.service.strategy;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.DevicesException;
import org.jumpmind.pos.devices.TestDevicesConfig;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest()
@AutoConfigureMockMvc
@ContextConfiguration(classes = { TestDevicesConfig.class })
public class ParseBusinessUnitIdFromDeviceIdStrategyTest {

    @Autowired
    ParseBusinessUnitIdFromDeviceIdStrategy strategy;

    @Test
    public void parseFromValidDeviceId() {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.setDeviceId("09999-001");
        assertEquals("09999", strategy.getBusinessUnitId(deviceModel));
    }

    @Test(expected = DevicesException.class)
    public void parseFromInvalidDeviceId() {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.setDeviceId("9999");
        strategy.getBusinessUnitId(deviceModel);
    }

}