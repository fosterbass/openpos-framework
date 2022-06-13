package org.jumpmind.pos.symds;


import org.apache.commons.lang3.BooleanUtils;
import org.jumpmind.pos.service.init.ModuleInitState;
import org.jumpmind.pos.service.init.ModuleInitStatus;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.IRegistrationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class SymDSInitStatusProviderTests {
    @Mock
    INodeService nodeService;

    @Mock
    IRegistrationService registrationService;

    @Mock
    Environment env;

    @Mock
    ISymmetricEngine symds;

    @InjectMocks
    SymDSInitStatusProvider fixture;

    AutoCloseable mocks;

    @Before
    public void initTestFixture() {
        mocks = MockitoAnnotations.openMocks(this);

        when(symds.getNodeService()).thenReturn(nodeService);
        when(symds.getRegistrationService()).thenReturn(registrationService);
    }

    @After
    public void cleanupTestFixture() throws Exception {
        mocks.close();
    }


    @Test
    public void ready_when_start_disabled() {
        mockConfigValues(false, true);
        mockNodeService(false, false);

        assertIsReady(fixture.getCurrentStatus());
    }


    @Test
    public void ready_when_wait_disabled() {
        mockConfigValues(true, false);
        mockNodeService(false, false);

        assertIsReady(fixture.getCurrentStatus());
    }


    @Test
    public void ready_when_registration_server() {
        mockConfigValues(true, true);
        mockNodeService(true, false);

        assertIsReady(fixture.getCurrentStatus());
    }

    @Test
    public void not_ready_when_not_registered_with_server() {
        mockConfigValues(true, true);
        mockNodeService(false, false);
        mockRegistrationService(false);

        assertIsNotReady(fixture.getCurrentStatus());
    }

    @Test
    public void not_ready_when_data_load_is_incomplete() {
        mockConfigValues(true, true);
        mockNodeService(false, false);
        mockRegistrationService(true);

        assertIsNotReady(fixture.getCurrentStatus());
    }

    @Test
    public void ready_when_enabled_and_data_load_is_complete() {
        mockConfigValues(true, true);
        mockNodeService(false, true);
        mockRegistrationService(true);

        assertIsReady(fixture.getCurrentStatus());
    }

    private void mockConfigValues(boolean start, boolean waitForInitialLoad) {
        when(env.getProperty(eq("openpos.symmetric.start"), any(String.class))).thenReturn(BooleanUtils.toStringTrueFalse(start));
        when(env.getProperty(eq("openpos.symmetric.waitForInitialLoad"), any(String.class))).thenReturn(BooleanUtils.toStringTrueFalse(waitForInitialLoad));

        when(env.getProperty(eq("openpos.symmetric.start"), any(Class.class))).thenReturn(BooleanUtils.toStringTrueFalse(start));
        when(env.getProperty(eq("openpos.symmetric.waitForInitialLoad"), any(Class.class))).thenReturn(BooleanUtils.toStringTrueFalse(waitForInitialLoad));
    }

    private void mockNodeService(boolean isRegistrationServer, boolean isDataLoadComplete) {
        when(nodeService.isRegistrationServer()).thenReturn(isRegistrationServer);
        when(nodeService.isDataLoadCompleted()).thenReturn(isDataLoadComplete);
    }

    private void mockRegistrationService(boolean isRegisteredWithServer) {
        when(registrationService.isRegisteredWithServer()).thenReturn(isRegisteredWithServer);
    }

    private static void assertIsReady(ModuleInitStatus actual) {
        assertEquals(ModuleInitState.READY, actual.getStatus());
    }

    private static void assertIsNotReady(ModuleInitStatus actual) {
        assertEquals(ModuleInitState.NOT_READY, actual.getStatus());
    }

    private static void assertErred(ModuleInitStatus actual) {
        assertEquals(ModuleInitState.ERROR, actual.getStatus());
    }
}
