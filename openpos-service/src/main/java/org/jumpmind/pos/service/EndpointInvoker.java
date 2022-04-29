package org.jumpmind.pos.service;

import org.jumpmind.pos.persist.DBSession;
import org.jumpmind.pos.service.filter.EndpointFilterManager;
import org.jumpmind.pos.service.instrumentation.ServiceSampleModel;
import org.jumpmind.pos.util.AppUtils;
import org.jumpmind.pos.util.SuppressMethodLogging;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * A worker which satisfies a pre-configured {@link EndpointInvocationContext} by performing a single service endpoint invocation and returning the
 * response.
 *
 * @see EndpointInvocationContext
 */
@Component
@Slf4j
public class EndpointInvoker {
    // TODO This should definitely not be non-private and non-final just so we can unit test its behavior.
    static ExecutorService instrumentationExecutor = newSingleThreadExecutor(new BasicThreadFactory.Builder()
            .namingPattern("service-instrumentation-thread-%d")
            .daemon(true)
            .build());

    private static final Histogram invocationDurationHistory = Histogram.build()
            .namespace("openpos")
            .name("endpoint_invocation_duration_seconds")
            .help("the amount of time spent invoking an endpoint")
            .exponentialBuckets(0.001, 2, 14)
            .labelNames("installation_id", "strategy", "service", "method", "result")
            .register();

    private static final String UNKNOWN = "UNKNOWN";
    private static final int MAX_SUMMARY_WIDTH = 127;

    private final Map<String, Boolean> endpointEnabledCache = new HashMap<>();

    @Autowired
    @Qualifier("ctxSession")
    @Lazy
    DBSession dbSession;

    @Autowired
    EndpointFilterManager endpointFilterManager;

    @Value("${openpos.installationId:'not set'}")
    String installationId;

    private final ThreadLocal<String> lastEndpointCalled = new ThreadLocal<>();
    private final ThreadLocal<EndpointInvocationContext> invocationPreferences = ThreadLocal.withInitial(EndpointInvocationContext::empty);

    /**
     * Invokes a service endpoint and returns the response.
     *
     * @param invocationContext the parameters dictating which endpoint to invoke and qualifying the invocation itself
     * @return the response received from the endpoint invocation described by {@code invocationContext}
     * @throws Throwable if any exceptions occur during the derivation of the endpoint invocation or during its execution
     */
    public Object invoke(EndpointInvocationContext invocationContext) throws Throwable {
        /* Resolve a final execution context.  The supplied context -- usually derived from static configuration and annotation directives -- will be
         * overridden by any corresponding values in the client-populated (and ephemeral) thread-local context. */
        final EndpointInvocationContext preferredInvocationContext = invocationContext.preferring(invocationPreferences.get());

        /* Clear out the thread-local context overrides, and do it NOW.  If we wait for this invocation to complete, and said invocation winds up
         * synchronously invoking other downstream services, we're going to inflict side effects on those delegated calls. */
        invocationPreferences.remove();

        final ServiceSampleModel sample = startSample(preferredInvocationContext);
        final Consumer<Boolean> observe = beginEndpointObservation(preferredInvocationContext);

        Object result;
        try {
            logInvocation(preferredInvocationContext);

            result = preferredInvocationContext.getStrategy().invoke(preferredInvocationContext);

            preferredInvocationContext.setResult(result);

            endpointFilterManager.filterResponse(preferredInvocationContext);
            result = preferredInvocationContext.getResult();

            endSampleSuccess(sample, preferredInvocationContext);

            observe.accept(true);
        }
        catch (Throwable ex) {
            endSampleError(sample, ex);
            observe.accept(false);

            throw ex;
        }
        return result;
    }

    /**
     * Updates an existing service invocation sample with a timestamp and the duration of the invocation before committing the sample to the
     * {@code service_sample} table.
     *
     * @param sample the service invocation sample to persist
     */
    void endSample(ServiceSampleModel sample) {
        if (sample != null) {
            sample.setEndTime(new Date());
            sample.setDurationMs(sample.getEndTime().getTime() - sample.getStartTime().getTime());

            instrumentationExecutor.execute(() -> dbSession.save(sample));
        }
    }

    /**
     * Commits a {@code service_sample} record reflecting a failed service invocation.
     *
     * @param sample the previously-initialized {@code service_sample} record describing the invocation
     * @param ex the exception which was thrown during the endpoint's invocation
     */
    void endSampleError(ServiceSampleModel sample, Throwable ex) {
        if (sample != null) {
            sample.setServiceResult(null);
            sample.setErrorFlag(true);
            sample.setErrorSummary(StringUtils.abbreviate(ex.getMessage(), MAX_SUMMARY_WIDTH));

            endSample(sample);
        }
    }

    /**
     * Commits a {@code service_sample} record reflecting a successful service invocation.
     *
     * @param sample the previously-initialized {@code service_sample} record describing the invocation
     * @param invocationContext the context describing the just-succeeded service invocation
     */
    void endSampleSuccess(ServiceSampleModel sample, EndpointInvocationContext invocationContext) {
        if ((sample != null) && (invocationContext.getResult() != null)) {
            sample.setServiceResult(StringUtils.abbreviate(invocationContext.getResult().toString(), MAX_SUMMARY_WIDTH));
        }
        endSample(sample);
    }

    /**
     * Invokes a service endpoint and returns the response.  Any values in the specified invocation context will override corresponding values in any
     * derived, deterministic invocation context <strong>for the duration of this invocation only</strong>.
     * <p>
     * This can be a little hard to follow because the relationship between this call and the actual invocation of an endpoint is obtuse.  When a
     * service endpoint method is called directly -- e.g. {@code fooService.getFoo()} -- the call is intercepted by proxy dispatcher
     * {@link EndpointInvocationHandler}.  That guy pre-populates an {@link EndpointInvocationContext} by interrogating the runtime Spring context and
     * the annotations on the service and corresponding endpoint classes/interfaces/methods.  It then passes that deterministic invocation context to
     * this invoker's {@link #invoke} method and the endpoint is invoked via the appropriate strategy-driven magic.
     * <p>
     * This variant of {@link #invoke}, however, puts its thumb on the scale by allowing the caller to override some aspects of the aforementioned
     * deterministic invocation context.  It does this not by modifying the signature of {@link EndpointInvocationHandler#invoke(Object, Method,
     * Object[])} (because that's illegal) but by (very) temporarily storing the client's preferred overrides here in a thread-local variable and then
     * delegating to the garden-variety service method invocation idiom.
     * <p>
     * It's assumed that {@code invocation} is a call to a proxied service method, and all such methods will, as discussed above, wind up being
     * intercepted by {@link EndpointInvocationHandler} and subsequently routed back to this class's {@link #invoke} method.  The latter method will,
     * in turn, give precedence to any overriding properties stored locally to the calling thread when deciding what the final invocation context
     * looks like.  That "merged" context will be passed downstream to the appropriate invocation strategy/strategies and synchronously return a
     * result from an endpoint, just as if the service method had been called directly.  At this point, it's safe to dispose of the client's preferred
     * overrides to the just-consumed invocation context.
     *
     * @param <T> the type of response expected from the invocation
     * @param invocation the service method call to invoke
     * @param invocationPreferences the client-specified overrides to {@code invocation's} deterministic invocation context
     * @return the response received from invoking {@code invocation}
     */
    <T> T invokePreferred(Supplier<T> invocation, EndpointInvocationContext invocationPreferences) {
        try {
            this.invocationPreferences.set(invocationPreferences);
            return invocation.get();
        }
        finally {
            /* This should have already been done in invoke(), but in case that call didn't complete normally, or if for whatever reason what we
             * executed wasn't a proxied service method, nuke our overriding context preferences from orbit again. */
            this.invocationPreferences.remove();
        }
    }

    /**
     * Determines whether sampling is enabled for a given endpoint invocation.  "Sampling" in this context refers to the persistence of auditing
     * information describing a service endpoint invocation and capturing its success or failure.
     *
     * @param invocationContext the context describing the endpoint to be invoked
     * @return {@code true} if {@code invocationContext} describes a service invocation to be sampled
     */
    boolean isSamplingEnabled(EndpointInvocationContext invocationContext) {
        /* The sampling determination is fixed by path, so reference a cache.  If no entry yet exists for this path, an endpoint will be determined to
         * be sampled if sampling is enabled for BOTH the service and for the to-be-invoked endpoint. */

        return endpointEnabledCache.computeIfAbsent(
                invocationContext.getEndpointPath(),
                path -> ofNullable(invocationContext.getConfig())
                        .filter(ServiceSpecificConfig::isSamplingEnabled)
                        .filter(config -> isNotEmpty(config.getEndpoints()))
                        .map(config -> config.getEndpoints().stream()
                                .filter(EndpointSpecificConfig::isSamplingEnabled)
                                .anyMatch(endpoint -> path.equals(endpoint.getPath())))
                        .orElse(false));
    }

    /**
     * Conditionally initializes an entity to be inserted into the {@code service_sample} table and representing an audit log entry capturing an
     * endpoint invocation.
     *
     * @param invocationContext the context describing the endpoint to be invoked
     * @return an entity to be inserted into the {@code service_sample} table to capture the details of invoking the endpoint described by
     * {@code invocationContext}
     */
    ServiceSampleModel startSample(EndpointInvocationContext invocationContext) {
        Method method = invocationContext.getMethod();

        if (isSamplingEnabled(invocationContext)) {
            final ServiceSampleModel serviceSample = new ServiceSampleModel();

            serviceSample.setSampleId(installationId + System.currentTimeMillis());
            serviceSample.setInstallationId(installationId);
            serviceSample.setHostname(AppUtils.getHostName());
            serviceSample.setServiceName(method.getDeclaringClass().getSimpleName() + "." + method.getName());
            serviceSample.setServiceType(invocationContext.getStrategy().getStrategyName());
            serviceSample.setStartTime(new Date());

            return serviceSample;
        }
        return null;
    }

    /**
     * @param invocationContext the context describing the endpoint to be invoked
     * @return a consumer which accepts a success/failure flag
     */
    private Consumer<Boolean> beginEndpointObservation(EndpointInvocationContext invocationContext) {
        // cannot integrate with the existing db based sampling because its configurable
        // and slightly invasive, just doing our own collection instead.
        final StopWatch sw = StopWatch.createStarted();

        final String instId = defaultIfEmpty(this.installationId, UNKNOWN);
        final String strategyName = defaultIfBlank(invocationContext.getStrategy().getStrategyName(), UNKNOWN);

        return success -> invocationDurationHistory
                .labels(
                        instId,
                        strategyName,
                        invocationContext.getMethod().getDeclaringClass().getSimpleName(),
                        invocationContext.getMethod().getName(),
                        (isTrue(success)) ? "SUCCESS" : "ERROR"
                )
                .observe(sw.getTime() / 1000.0);
    }

    /**
     * Writes a log entry capturing a service endpoint invocation.
     *
     * @param invocationContext the context describing the endpoint to be invoked
     */
    private void logInvocation(EndpointInvocationContext invocationContext) {
        final Method method = invocationContext.getMethod();

        /*
         * Write a log entry if all three of the following are true:
         *
         * (1) the local logger has a threshold of INFO or higher
         * (2) the method representing the endpoint is not annotated with @SuppressMethodLogging
         * (3) the endpoint being invoked isn't identical to the most recently-logged and -invoked endpoint
         */
        if (log.isInfoEnabled() && !method.isAnnotationPresent(SuppressMethodLogging.class)) {
            final String implementation = invocationContext.getEndpointImplementation();
            final String endPointCalled = String.format(
                    "%s.%s() %s",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    ((implementation == null) || Endpoint.IMPLEMENTATION_DEFAULT.equals(implementation))
                            ? ""
                            : implementation + " implementation"
            );

            if (!endPointCalled.equals(lastEndpointCalled.get())) {
                log.info(endPointCalled);
                this.lastEndpointCalled.set(endPointCalled);
            }
        }
    }
}
