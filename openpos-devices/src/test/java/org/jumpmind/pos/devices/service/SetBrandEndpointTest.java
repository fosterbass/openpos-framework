package org.jumpmind.pos.devices.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jumpmind.pos.devices.TestDevicesConfig;
import org.jumpmind.pos.devices.service.model.SetBrandRequest;
import org.jumpmind.pos.devices.service.model.SetBrandResponse;
import org.jumpmind.pos.test.MockPutRequestBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"test", "test-business-unit-provider"})
@AutoConfigureMockMvc
@ContextConfiguration(classes = {TestDevicesConfig.class})
public class SetBrandEndpointTest {
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mvc;

    @Test
    public void getDeviceShouldReturnMatchingDevice() throws Exception {

        String result =
                mvc.perform(
                                new MockPutRequestBuilder("/devices/setBrand")
                                        .content(
                                                SetBrandRequest.builder()
                                                        .deviceId("00100-001")
                                                        .newBrand("newBrand")
                                                        .build())
                                        .build())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        assertEquals("newBrand", mapper.readValue(result, SetBrandResponse.class).getDevice().getBrand());
    }

    @Test()
    public void getDeviceShouldRespondWithNotFound() throws Exception {
        mvc.perform(new MockPutRequestBuilder("/devices/setBrand")
                        .content(
                                SetBrandRequest.builder()
                                        .deviceId("xxxxx")
                                        .newBrand("newBrand")
                                        .build())
                        .build())
                .andExpect(status().is5xxServerError());
    }
}
