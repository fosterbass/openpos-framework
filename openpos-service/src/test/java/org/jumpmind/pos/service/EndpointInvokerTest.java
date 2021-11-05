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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EndpointInvokerTest {

    @SuppressMethodLogging
    public void TestMethodNotAnnotated() {}

    @Sample
    public void TestMethodAnnotated() {}

    String installationId = "TestInstallationId";

    private ServiceSpecificConfig getServiceSpecificConfig() {
        ServiceSpecificConfig config = new ServiceSpecificConfig();
        SamplingConfig sampleConfig = new SamplingConfig();
        sampleConfig.setEnabled(true);
        config.setSamplingConfig(sampleConfig);

        EndpointSpecificConfig endpointSpecificConfig = new EndpointSpecificConfig();
        endpointSpecificConfig.setPath("/test/one");
        endpointSpecificConfig.setSamplingConfig(sampleConfig);

        List<EndpointSpecificConfig> endpoints = new ArrayList<>();
        endpoints.add(endpointSpecificConfig);

        config.setEndpoints( endpoints);
        return config;
    }

    @Test
    public void invokeStrategySamplesAndCallsInvokeTest() throws Throwable {
        IInvocationStrategy invocationStrategy = mock(LocalOnlyStrategy.class);
        List<String> profileIds = new ArrayList<>();
        Method method = EndpointInvokerTest.class.getMethod("TestMethodNotAnnotated");

        ServiceSpecificConfig config = getServiceSpecificConfig();
        String path = "/test/one";

        DBSession session = mock(DBSession.class);
        EndpointFilterManager endpointFilterManager = mock(EndpointFilterManager.class);

        EndpointInvoker endpointInvoker = spy(new EndpointInvoker());
        endpointInvoker.dbSession = session;
        endpointInvoker.endpointFilterManager = endpointFilterManager;

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .profileIds(profileIds)
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .clientVersionString("@version")
                .build();

        doReturn(new Object()).when(invocationStrategy).invoke(endpointInvocationContext);

        Object result = endpointInvoker.invokeStrategy(endpointInvocationContext);

        verify(endpointInvoker, atLeastOnce()).startSample(endpointInvocationContext);
        verify(invocationStrategy, atLeastOnce()).invoke(endpointInvocationContext);
        verify(endpointInvoker, atLeastOnce()).endSampleSuccess(anyObject(), eq(endpointInvocationContext));
        assertNotNull(result, "invokeStrategy should not return null in this case.");
    }

    @Test
    public void invokeStrategyEndsSampleAndThrowsTest() throws Throwable {
        IInvocationStrategy invocationStrategy = mock(LocalOnlyStrategy.class);
        List<String> profileIds = new ArrayList<>();
        Method method = EndpointInvokerTest.class.getMethod("TestMethodNotAnnotated");

        ServiceSpecificConfig config = getServiceSpecificConfig();

        String path = "/test/one";

        DBSession session = mock(DBSession.class);
        EndpointInvoker endpointInvoker = spy(new EndpointInvoker());
        endpointInvoker.dbSession = session;

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .profileIds(profileIds)
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .build();

        Exception exception = new Exception();
        doThrow(exception).when(invocationStrategy).invoke(endpointInvocationContext);

        try{
            Object result = endpointInvoker.invokeStrategy(endpointInvocationContext);
        } catch (Throwable ex) {
            verify(endpointInvoker, atLeastOnce()).startSample(endpointInvocationContext);
            verify(invocationStrategy, atLeastOnce()).invoke(endpointInvocationContext);
            verify(endpointInvoker, never()).endSampleSuccess(anyObject(), eq(endpointInvocationContext));
            verify(endpointInvoker, atLeastOnce()).endSampleError(anyObject(), anyObject());
            if (!ex.equals(exception)) {
                throw ex;
            }
        }
    }

    private ServiceSampleModel getServiceSampleModel() {
        ServiceSampleModel sampleModel = mock(ServiceSampleModel.class, RETURNS_DEEP_STUBS);
        sampleModel.setStartTime(new Date());
        sampleModel.setEndTime(new Date());
        return sampleModel;
    }

    @Test
    public void startSampleReturnsNullWhenNotConfiguredAtModuleLevelToSampleMethodTest() throws NoSuchMethodException {
        IInvocationStrategy invocationStrategy = new LocalOnlyStrategy();

        Method method = EndpointInvokerTest.class.getMethod("TestMethodNotAnnotated");

        ServiceSpecificConfig config = getServiceSpecificConfig();
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

    @Test
    public void startSampleReturnsNullWhenNotConfiguredAtEndpointLevelToSampleMethodTest() throws NoSuchMethodException {
        IInvocationStrategy invocationStrategy = new LocalOnlyStrategy();

        Method method = EndpointInvokerTest.class.getMethod("TestMethodNotAnnotated");

        ServiceSpecificConfig config = getServiceSpecificConfig();
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
    public void startSampleReturnsNotNullWhenIsConfiguredToSampleMethodTest() throws NoSuchMethodException {
        IInvocationStrategy invocationStrategy = new LocalOnlyStrategy();

        Method method = EndpointInvokerTest.class.getMethod("TestMethodAnnotated");

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        endpointInvoker.installationId = installationId;

        ServiceSpecificConfig config = getServiceSpecificConfig();

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
    public void isSamplingEnabledAddsUnseenPathsToCache(){
        String path = "/test/one";
        ServiceSpecificConfig serviceSpecificConfig = serviceSpecificConfigSetup(path);

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        HashMap<String, Boolean> endpointCache = spy(new HashMap<>());
        endpointInvoker.endpointEnabledCache = endpointCache;

        boolean result = endpointInvoker.isSamplingEnabled(path, serviceSpecificConfig);

        verify(endpointCache, atLeastOnce()).get(path);
        verify(endpointCache, atLeastOnce()).put(path, true);
        assertEquals(1, endpointCache.size());
        assertEquals(true, result);
    }

    @Test
    public void isSamplingEnabledDoseNotAddSeenPathsToCache(){
        String path = "/test/one";
        ServiceSpecificConfig serviceSpecificConfig = serviceSpecificConfigSetup(path);

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        HashMap<String, Boolean> endpointCache = spy(new HashMap<>());
        endpointInvoker.endpointEnabledCache = endpointCache;

        endpointInvoker.isSamplingEnabled(path, serviceSpecificConfig);
        boolean result = endpointInvoker.isSamplingEnabled(path, serviceSpecificConfig);

        verify(endpointCache, atLeastOnce()).get(path);
        verify(endpointCache, atLeastOnce()).put(path, true);
        assertEquals(1, endpointCache.size());
        assertEquals(true, result);
    }

    @Test
    public void isSamplingEnabledDoseNotAddDisabledEndpointsAtEndpointLevel(){
        String path = "/test/one";
        ServiceSpecificConfig serviceSpecificConfig = serviceSpecificConfigSetup(path);

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        HashMap<String, Boolean> endpointCache = spy(new HashMap<>());
        endpointInvoker.endpointEnabledCache = endpointCache;

        serviceSpecificConfig.getEndpoints().get(0).getSamplingConfig().setEnabled(false);
        boolean result = endpointInvoker.isSamplingEnabled(path, serviceSpecificConfig);

        serviceSpecificConfig.getEndpoints().get(0).getSamplingConfig().setEnabled(true);
        serviceSpecificConfig.getSamplingConfig().setEnabled(false);
        endpointInvoker.isSamplingEnabled(path, serviceSpecificConfig);

        verify(endpointCache, atLeastOnce()).get(path);
        verify(endpointCache, atLeastOnce()).put(path, false);
        assertEquals(1, endpointCache.size());
        assertEquals(false, result);
    }

    @Test
    public void isSamplingEnabledDoseNotAddDisabledEndpointsAtModuleLevel(){
        String path = "/test/one";
        ServiceSpecificConfig serviceSpecificConfig = serviceSpecificConfigSetup(path);

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        HashMap<String, Boolean> endpointCache = spy(new HashMap<>());
        endpointInvoker.endpointEnabledCache = endpointCache;

        serviceSpecificConfig.getSamplingConfig().setEnabled(false);
        boolean result = endpointInvoker.isSamplingEnabled(path, serviceSpecificConfig);

        verify(endpointCache, atLeastOnce()).get(path);
        verify(endpointCache, atLeastOnce()).put(path, false);
        assertEquals(1, endpointCache.size());
        assertEquals(false, result);
    }

    @Test
    public void isSamplingEnabledReturnsNullWithConfigNull(){
        String path = "/test/one";

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        HashMap<String, Boolean> endpointCache = spy(new HashMap<>());
        endpointInvoker.endpointEnabledCache = endpointCache;

        boolean result = endpointInvoker.isSamplingEnabled(path, null);

        verify(endpointCache, atLeastOnce()).get(path);
        verify(endpointCache, atLeastOnce()).put(path, false);
        assertEquals(1, endpointCache.size());
        assertEquals(false, result);
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

    @Test
    public void startSampleReturnsAcceptableFieldsTest() throws NoSuchMethodException {
        IInvocationStrategy invocationStrategy = new LocalOnlyStrategy();

        Method method = EndpointInvokerTest.class.getMethod("TestMethodAnnotated");
        String simpleName = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        EndpointInvoker endpointInvoker = new EndpointInvoker();
        endpointInvoker.installationId = installationId;

        ServiceSpecificConfig config = getServiceSpecificConfig();

        String path = "/test/one";

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .strategy(invocationStrategy)
                .config(config)
                .method(method)
                .endpointPath(path)
                .build();

        ServiceSampleModel result = endpointInvoker.startSample(endpointInvocationContext);

        assertTrue(Pattern.matches(installationId + "\\d*", result.getSampleId()), "sampleId should have installationId followed by the system time in milliseconds.");
        assertEquals(installationId, result.getInstallationId(), "installationId should be populated with the installationId.");
        assertNotNull(result.getHostname(), "hostname should populate results with the systems hostname.");
        assertTrue(Pattern.matches(simpleName + "." + methodName, result.getServiceName()), "serviceName should be populated with the methodDeclaringClassSimpleName.methodName.");
        assertEquals(invocationStrategy.getStrategyName(), result.getServiceType(), "serviceType should be populated with the strategy name of the strategy passed.");
        assertNotNull(result.getStartTime(), "startTime should be populated with the data the approximate date the method was called.");
        assertNull(result.getServicePath(), "servicePath should not be set by startSample.");
        assertNull(result.getServiceResult(), "serviceResult should not be set by startSample.");
        assertNull(result.getEndTime(), "serviceResult should not be set by startSample.");
        assertEquals( 0, result.getDurationMs(), "durationMS should not be set by startSample.");
        assertFalse(result.isErrorFlag(), "errorFlag should not be set by startSample.");
        assertNull(result.getErrorSummary(), "errorSummary should not be set by startSample.");
    }

    @Test
    public void endSampleSuccessEndsSampleTest() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        ServiceSampleModel sampleModel = getServiceSampleModel();

        Object testResult = new Object();
        DBSession session = mock(DBSession.class);

        EndpointInvoker endpointInvoker = spy(new EndpointInvoker());
        endpointInvoker.dbSession = session;

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder()
                .result("result").build();

        endpointInvoker.endSampleSuccess(sampleModel, endpointInvocationContext);

        verify(sampleModel, atLeastOnce()).setServiceResult(anyString());
        verify(endpointInvoker, atLeastOnce()).endSample(sampleModel);
    }

    @Test
    public void endSampleErrorProperlySetsFieldsTest() throws NoSuchMethodException {
        ServiceSampleModel sampleModel = getServiceSampleModel();

        Object testResult = new Object();
        DBSession session = mock(DBSession.class);

        EndpointInvoker endpointInvoker = spy(new EndpointInvoker());
        endpointInvoker.dbSession = session;

        EndpointInvocationContext endpointInvocationContext = EndpointInvocationContext.builder().build();

        endpointInvoker.endSampleError(sampleModel, new Exception("Test"));

        verify(sampleModel, atLeastOnce()).setServiceResult(null);
        verify(sampleModel, atLeastOnce()).setErrorFlag(true);
        verify(sampleModel, atLeastOnce()).setErrorSummary(anyString());
        verify(endpointInvoker, atLeastOnce()).endSample(sampleModel);
    }

    @Test
    public void endSampleSetsDataAndSavesToDBSession() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        ServiceSampleModel sampleModel = getServiceSampleModel();

        EndpointInvoker endpointInvoker = new EndpointInvoker();

        Field executor = EndpointInvoker.class.getDeclaredField("instrumentationExecutor");
        executor.setAccessible(true);

// This trick does not work on later versions of java.  Making EndpointInvoker.instrumentationExecutor non final
//        Field modifiers = Field.class.getDeclaredField("modifiers");
//        modifiers.setAccessible(true);
//        modifiers.setInt(executor, executor.getModifiers() & ~Modifier.FINAL);

        ExecutorService executorService = mock(ExecutorService.class);
        executor.set(endpointInvoker, executorService);

        endpointInvoker.endSample(sampleModel);

        verify(sampleModel, atLeastOnce()).setEndTime(anyObject());
        verify(sampleModel, atLeastOnce()).setDurationMs(anyLong());
        verify(executorService, atLeastOnce()).execute(anyObject());
    }
}