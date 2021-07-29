package org.jumpmind.pos.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceSpecificConfigTest {

    String modulePath;
    String endpointPath;
    String serviceTestId;
    int endpointIndex;
    SamplingConfig samplingConfig;
    EndpointSpecificConfig endpointSpecificConfig;


    ServiceSpecificConfig serviceSpecificConfig = new ServiceSpecificConfig();

    @Mock
    IConfigApplicator iConfigApplicator;

    @Before
    public void before(){
        MockitoAnnotations.initMocks(this);
        serviceTestId = "TestID";
        endpointIndex = 0;
        modulePath = String.format("openpos.services.specificConfig.%s.samplingConfig", serviceTestId);
        endpointPath = String.format("openpos.services.specificConfig.%s.endpoints[%d]", serviceTestId, endpointIndex);
        serviceSpecificConfig.additionalConfigSource = iConfigApplicator;

        samplingConfig = new SamplingConfig();
        serviceSpecificConfig.samplingConfig = samplingConfig;

        List<EndpointSpecificConfig> endpointSpecificConfigList = new ArrayList<>();
        endpointSpecificConfig = new EndpointSpecificConfig();
        endpointSpecificConfigList.add(endpointSpecificConfig);
        serviceSpecificConfig.setEndpoints(endpointSpecificConfigList);
    }

    @Test
    public void findAdditionalConfigsWithNullSampleConfig() {
        serviceSpecificConfig.samplingConfig = null;
        serviceSpecificConfig.findAdditionalConfigs(serviceTestId);
        verify(iConfigApplicator, atLeastOnce()).applyAdditionalConfiguration(eq(modulePath), any(SamplingConfig.class));
        verify(iConfigApplicator, atLeastOnce()).applyAdditionalConfiguration(eq(endpointPath), eq(endpointSpecificConfig));
    }

    @Test
    public void findAdditionalConfigsWithNullAdditionalConfigSource() {
        serviceSpecificConfig.additionalConfigSource = null;
        serviceSpecificConfig.findAdditionalConfigs(serviceTestId);
        verify(iConfigApplicator, never()).applyAdditionalConfiguration(eq(modulePath), eq(samplingConfig));
        verify(iConfigApplicator, never()).applyAdditionalConfiguration(eq(endpointPath), eq(EndpointSpecificConfig.class));
    }
}