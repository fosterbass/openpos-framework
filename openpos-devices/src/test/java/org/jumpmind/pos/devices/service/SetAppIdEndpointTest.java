package org.jumpmind.pos.devices.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jumpmind.pos.devices.TestDevicesConfig;
import org.jumpmind.pos.devices.service.model.SetAppIdRequest;
import org.jumpmind.pos.devices.service.model.SetAppIdResponse;
import org.jumpmind.pos.test.MockPostRequestBuilder;
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
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration(classes = {TestDevicesConfig.class})
public class SetAppIdEndpointTest {
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mvc;

    @Test
    public void getDeviceShouldReturnMatchingDevice() throws Exception {

        String result =
                mvc.perform(
                                new MockPostRequestBuilder("/devices/setAppId")
                                        .content(
                                                SetAppIdRequest.builder()
                                                        .deviceId("00100-001")
                                                        .newAppId("newAppId")
                                                        .build())
                                        .build())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        assertEquals("newAppId", mapper.readValue(result, SetAppIdResponse.class).getDevice().getAppId());
    }

    @Test()
    public void getDeviceShouldRespondWithNotFound() throws Exception {
        mvc.perform(new MockPostRequestBuilder("/devices/setAppId")
                        .content(
                                SetAppIdRequest.builder()
                                        .deviceId("xxxxx")
                                        .newAppId("newAppId")
                                        .build())
                        .build())
                .andExpect(status().is5xxServerError());
    }
}
