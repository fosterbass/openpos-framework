package org.jumpmind.pos.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

import static java.util.Collections.singletonList;

@RunWith(MockitoJUnitRunner.class)
public class ServiceSpecificConfigTest {
    private ServiceSpecificConfig serviceSpecificConfig;

    private String modulePath;
    private String endpointPath;
    private String serviceTestId;
    private SamplingConfig samplingConfig;
    private EndpointSpecificConfig endpointSpecificConfig;

    @Mock
    private IConfigApplicator iConfigApplicator;

    @Before
    public void before() {
        serviceSpecificConfig = new ServiceSpecificConfig();

        MockitoAnnotations.openMocks(this);
        serviceTestId = "TestID";
        modulePath = String.format("openpos.services.specificConfig.%s.samplingConfig", serviceTestId);
        endpointPath = String.format("openpos.services.specificConfig.%s.endpoints[%d]", serviceTestId, 0);
        serviceSpecificConfig.additionalConfigSource = iConfigApplicator;

        samplingConfig = new SamplingConfig();
        serviceSpecificConfig.setSamplingConfig(samplingConfig);

        endpointSpecificConfig = new EndpointSpecificConfig();
        serviceSpecificConfig.setEndpoints(singletonList(endpointSpecificConfig));
    }

    @Test
    public void findAdditionalConfigsWithNullSampleConfig() {
        serviceSpecificConfig.setSamplingConfig(null);
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
