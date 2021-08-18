package org.jumpmind.pos.devices.service.strategy;

import org.jumpmind.pos.devices.TestDevicesConfig;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class GetBusinessUnitIdFromConfigStrategyTest {
    @Autowired
    GetBusinessUnitIdFromConfigStrategy strategy;

    @Value("${openpos.businessunitId:undefined}")
    private String expectedBusinessUnitId;

    @Test
    public void ensureBusinessUnitIdReadFromConfig() {
        assertEquals(expectedBusinessUnitId, strategy.getBusinessUnitId(new DeviceModel()));
    }

}