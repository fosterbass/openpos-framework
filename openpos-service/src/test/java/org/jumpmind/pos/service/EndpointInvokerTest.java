package org.jumpmind.pos.service;

import org.jumpmind.pos.persist.DBSession;
import org.jumpmind.pos.service.filter.EndpointFilterManager;
import org.jumpmind.pos.service.instrumentation.Sample;
import org.jumpmind.pos.service.instrumentation.ServiceSampleModel;
import org.jumpmind.pos.service.strategy.IInvocationStrategy;
import org.jumpmind.pos.service.strategy.LocalOnlyStrategy;
import org.jumpmind.pos.util.SuppressMethodLogging;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static java.util.Collections.emptyList;

@RunWith(MockitoJUnitRunner.class)
public class EndpointInvokerTest {
    // TODO The class under test should be @Autowired and its collaborators injected as mocks.
    // TODO Rewrite tests with captors for EndpointInvocationContexts passed to the strategies.  They're no longer what's passed to EndpointInvoker.
    /* TODO Clean this up.  Many of these aren't unit tests and are instead fragile doppelgangers for the internal implementation details of the class
     * under test.  If we need to verify a DB record was written, we shouldn't be spying on the executor which calls "save" on the session and
     * verifying that the former's execute() method was called.  That test will break as soon as we de-thread the save call or thread it through any
     * other mechanism, even though the DB record will still be getting written.  Instead, we should be mocking the injected session and verifying
     * its save() method was called. */

    private static final String installationId = "TestInstallationId";

    @Sample
    public void TestMethodAnnotated() {}

    @SuppressMethodLogging
    public void TestMethodNotAnnotated() {}

    @Test
    public void endSampleErrorProperlySetsFieldsTest() {
        final ServiceSampleModel sampleModel = mockServiceSampleModel();

        final EndpointInvoker endpointInvoker = spy(new EndpointInvoker());
        endpointInvoker.dbSession = mock(DBSession.class);

        endpointInvoker.endSampleError(sampleModel, new Exception("Test"));

        verify(sampleModel, atLeastOnce()).setServiceResult(null);
        verify(sampleModel, atLeastOnce()).setErrorFlag(true);
        verify(sampleModel, atLeastOnce()).setErrorSummary(anyString());
        verify(endpointInvoker, atLeastOnce()).endSample(sampleModel);
    }

    @Test
    public void endSampleSetsDataAndSavesToDBSession() throws NoSuchFieldException, IllegalAccessException {
        final ServiceSampleModel sampleModel = mockServiceSampleModel();
        final EndpointInvoker endpointInvoker = new EndpointInvoker();

        final ExecutorService executorService = mock(ExecutorService.class);
        final Field executor = EndpointInvoker.class.getDeclaredField("instrumentationExecutor");
        executor.setAccessible(true);
        executor.set(endpointInvoker, executorService);

        endpointInvoker.endSample(sampleModel);

        verify(sampleModel, atLeastOnce()).setEndTime(any());
        verify(sampleModel, atLeastOnce()).setDurationMs(anyLong());
        verify(executorService, atLeastOnce()).execute(any());
    }

    @Test
    public void endSampleSuccessEndsSampleTest() {
        final ServiceSampleModel sampleModel = mockServiceSampleModel();
        final DBSession session = mock(DBSession.class);

        final EndpointInvoker endpointInvoker = spy(new EndpointInvoker());
        endpointInvoker.dbSession = session;

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .result("result").build();

        endpointInvoker.endSampleSuccess(sampleModel, endpointInvocationContext);

        verify(sampleModel, atLeastOnce()).setServiceResult(anyString());
        verify(endpointInvoker, atLeastOnce()).endSample(sampleModel);
    }

    @Test
    public void invokeEndsSampleAndThrowsTest() throws Throwable {
        final IInvocationStrategy invocationStrategy = mock(LocalOnlyStrategy.class);
        final Method method = EndpointInvokerTest.class.getMethod("TestMethodNotAnnotated");
        final ServiceSpecificConfig config = createServiceSpecificConfig();
        final String path = "/test/one";

        final EndpointInvoker endpointInvoker = spy(new EndpointInvoker());
        endpointInvoker.dbSession = mock(DBSession.class);
        endpointInvoker.endpointFilterManager = mock(EndpointFilterManager.class);

        final EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .profileIds(emptyList())
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .build();

        final Exception exception = new Exception();

        // We can't assume the provided invocation context was preserved as-is by the invoker.
        doReturn("LOCAL_ONLY").when(invocationStrategy).getStrategyName();
        lenient().doThrow(exception).when(invocationStrategy).invoke(endpointInvocationContext);

        try {
            endpointInvoker.invoke(endpointInvocationContext);
        }
        catch (Throwable ex) {
            verify(endpointInvoker, atLeastOnce()).startSample(any(EndpointInvocationContext.class));
            verify(invocationStrategy, atLeastOnce()).invoke(any(EndpointInvocationContext.class));
            verify(endpointInvoker, never()).endSampleSuccess(any(), any(EndpointInvocationContext.class));
            verify(endpointInvoker, atLeastOnce()).endSampleError(any(), any());

            if (!ex.equals(exception)) {
                throw ex;
            }
        }
    }

    @Test
    public void invokeSamplesAndCallsInvokeTest() throws Throwable {
        final IInvocationStrategy invocationStrategy = mock(LocalOnlyStrategy.class);
        final Method method = EndpointInvokerTest.class.getMethod("TestMethodNotAnnotated");
        final ServiceSpecificConfig config = createServiceSpecificConfig();
        final String path = "/test/one";

        final EndpointInvoker endpointInvoker = spy(new EndpointInvoker());
        endpointInvoker.dbSession = mock(DBSession.class);
        endpointInvoker.endpointFilterManager = mock(EndpointFilterManager.class);

        final EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .profileIds(emptyList())
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .clientVersionString("@version")
                .build();

        // We can't assume the provided invocation context was preserved as-is by the invoker.
        doReturn(new Object()).when(invocationStrategy).invoke(any(EndpointInvocationContext.class));

        final Object result = endpointInvoker.invoke(endpointInvocationContext);

        verify(endpointInvoker, atLeastOnce()).startSample(any(EndpointInvocationContext.class));
        verify(invocationStrategy, atLeastOnce()).invoke(any(EndpointInvocationContext.class));
        verify(endpointInvoker, atLeastOnce()).endSampleSuccess(any(), any(EndpointInvocationContext.class));
        assertNotNull(result, "invoke should not return null in this case.");
    }

    @Test
    public void isSamplingEnabledDoseNotAddDisabledEndpointsAtEndpointLevel() {
        final String path = "/test/one";
        final ServiceSpecificConfig serviceSpecificConfig = serviceSpecificConfigSetup(path);
        final EndpointInvoker endpointInvoker = new EndpointInvoker();

        serviceSpecificConfig.getEndpoints().get(0).getSamplingConfig().setEnabled(false);

        assertFalse(endpointInvoker.isSamplingEnabled(createInvocationContext(path, serviceSpecificConfig)));

        serviceSpecificConfig.getEndpoints().get(0).getSamplingConfig().setEnabled(true);
        serviceSpecificConfig.getSamplingConfig().setEnabled(false);

        assertFalse(endpointInvoker.isSamplingEnabled(createInvocationContext(path, serviceSpecificConfig)));
    }

    @Test
    public void isSamplingEnabledDoseNotAddDisabledEndpointsAtModuleLevel() {
        final String path = "/test/one";
        final ServiceSpecificConfig serviceSpecificConfig = serviceSpecificConfigSetup(path);
        serviceSpecificConfig.getSamplingConfig().setEnabled(false);

        final boolean result = new EndpointInvoker().isSamplingEnabled(createInvocationContext(path, serviceSpecificConfig));

        assertFalse(result);
    }

    @Test
    public void isSamplingEnabledReturnsNullWithConfigNull() {
        assertFalse(new EndpointInvoker().isSamplingEnabled(createInvocationContext("/test/one", null)));
    }

    @Test
    public void startSampleReturnsAcceptableFieldsTest() throws NoSuchMethodException {
        IInvocationStrategy invocationStrategy = new LocalOnlyStrategy();

        Method method = EndpointInvokerTest.class.getMethod("TestMethodAnnotated");
        String simpleName = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        endpointInvoker.installationId = installationId;

        ServiceSpecificConfig config = createServiceSpecificConfig();

        String path = "/test/one";

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .build();

        ServiceSampleModel result = endpointInvoker.startSample(endpointInvocationContext);

        assertTrue(Pattern.matches(installationId + "\\d*", result.getSampleId()),
                "sampleId should have installationId followed by the system time in milliseconds.");
        assertEquals(installationId, result.getInstallationId(), "installationId should be populated with the installationId.");
        assertNotNull(result.getHostname(), "hostname should populate results with the systems hostname.");
        assertTrue(Pattern.matches(simpleName + "." + methodName, result.getServiceName()),
                "serviceName should be populated with the methodDeclaringClassSimpleName.methodName.");
        assertEquals(invocationStrategy.getStrategyName(), result.getServiceType(),
                "serviceType should be populated with the strategy name of the strategy passed.");
        assertNotNull(result.getStartTime(), "startTime should be populated with the data the approximate date the method was called.");
        assertNull(result.getServicePath(), "servicePath should not be set by startSample.");
        assertNull(result.getServiceResult(), "serviceResult should not be set by startSample.");
        assertNull(result.getEndTime(), "serviceResult should not be set by startSample.");
        assertEquals(0, result.getDurationMs(), "durationMS should not be set by startSample.");
        assertFalse(result.isErrorFlag(), "errorFlag should not be set by startSample.");
        assertNull(result.getErrorSummary(), "errorSummary should not be set by startSample.");
    }

    @Test
    public void startSampleReturnsNotNullWhenIsConfiguredToSampleMethodTest() throws NoSuchMethodException {
        IInvocationStrategy invocationStrategy = new LocalOnlyStrategy();

        Method method = EndpointInvokerTest.class.getMethod("TestMethodAnnotated");

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        endpointInvoker.installationId = installationId;

        ServiceSpecificConfig config = createServiceSpecificConfig();

        String path = "/test/one";

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .build();

        ServiceSampleModel result = endpointInvoker.startSample(endpointInvocationContext);

        assertNotNull(result, "EndpointInvoker.startSample should return not null when the method passed is configured to be sampled.");
    }

    @Test
    public void startSampleReturnsNullWhenNotConfiguredAtEndpointLevelToSampleMethodTest() throws NoSuchMethodException {
        IInvocationStrategy invocationStrategy = new LocalOnlyStrategy();

        Method method = EndpointInvokerTest.class.getMethod("TestMethodNotAnnotated");

        ServiceSpecificConfig config = createServiceSpecificConfig();
        config.getEndpoints().get(0).getSamplingConfig().setEnabled(false);

        String path = "/test/one";

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .build();

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        ServiceSampleModel result = endpointInvoker.startSample(endpointInvocationContext);
        assertNull(result, "EndpointInvoker.startSample should return null when the method passed is not configured to be sampled");
    }

    @Test
    public void startSampleReturnsNullWhenNotConfiguredAtModuleLevelToSampleMethodTest() throws NoSuchMethodException {
        IInvocationStrategy invocationStrategy = new LocalOnlyStrategy();

        Method method = EndpointInvokerTest.class.getMethod("TestMethodNotAnnotated");

        ServiceSpecificConfig config = createServiceSpecificConfig();
        config.getSamplingConfig().setEnabled(false);

        String path = "/test/one";

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .build();

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        ServiceSampleModel result = endpointInvoker.startSample(endpointInvocationContext);
        assertNull(result, "EndpointInvoker.startSample should return null when the method passed is not configured to be sampled");
    }

    private EndpointInvocationContext createInvocationContext(String endpointPath, ServiceSpecificConfig serviceConfig) {
        return EndpointInvocationContext.builder()
                .endpointPath(endpointPath)
                .config(serviceConfig)
                .build();
    }

    private ServiceSpecificConfig createServiceSpecificConfig() {
        ServiceSpecificConfig config = new ServiceSpecificConfig();
        SamplingConfig sampleConfig = new SamplingConfig();
        sampleConfig.setEnabled(true);
        config.setSamplingConfig(sampleConfig);

        EndpointSpecificConfig endpointSpecificConfig = new EndpointSpecificConfig();
        endpointSpecificConfig.setPath("/test/one");
        endpointSpecificConfig.setSamplingConfig(sampleConfig);

        List<EndpointSpecificConfig> endpoints = new ArrayList<>();
        endpoints.add(endpointSpecificConfig);

        config.setEndpoints(endpoints);
        return config;
    }

    private ServiceSampleModel mockServiceSampleModel() {
        ServiceSampleModel sampleModel = mock(ServiceSampleModel.class, RETURNS_DEEP_STUBS);

        sampleModel.setStartTime(new Date());
        sampleModel.setEndTime(new Date());

        return sampleModel;
    }

    private ServiceSpecificConfig serviceSpecificConfigSetup(String path) {
        ServiceSpecificConfig serviceSpecificConfig = new ServiceSpecificConfig();
        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setEnabled(true);
        serviceSpecificConfig.setSamplingConfig(samplingConfig);

        List<EndpointSpecificConfig> endpoints = new ArrayList<>();
        EndpointSpecificConfig endpointSpecificConfig = new EndpointSpecificConfig();
        endpointSpecificConfig.setPath(path);
        endpointSpecificConfig.setSamplingConfig(samplingConfig);
        endpoints.add(endpointSpecificConfig);

        serviceSpecificConfig.setEndpoints(endpoints);

        return serviceSpecificConfig;
    }
}
