package org.jumpmind.pos.service;

import org.jumpmind.pos.service.EndpointInvocationContext.EndpointInvocationContextBuilder;
import org.jumpmind.pos.service.strategy.IInvocationStrategy;
import org.jumpmind.pos.service.strategy.InvocationStrategy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

import static org.jumpmind.pos.service.strategy.InvocationStrategy.*;

/**
 * An API for identifying, configuring, and invoking service endpoints.  Typically, a client would invoke an endpoint's corresponding service method
 * directly; e.g.:
 * <pre>
 *     FooResponse response = fooService.getFoo(new FooRequest(...));
 * </pre>
 * <p>
 * In this common use case, the framework-level behavior of the invocation -- the resolution of an endpoint class and method, the determination of
 * which strategy or strategies to employ, etc. -- is dictated entirely through "static" directives: profile-driven Spring configurations and class-
 * and method-annotated instructions.  The invoking client neither possesses nor desires to possess any control over those behaviors, as each
 * invocation should behave, at a domain-agnostic level, identically to every other of its peer invocations.
 * <p>
 * Occasionally, however, the client caller requires the ability to change these statically-defined behaviors at the point of invocation, and that's
 * where an {@code EndpointClient} comes into play.  Using an instance of this client (which is stateless and should be injected as a singleton
 * bean), a consumer can defer a service invocation until whatever alterations to the latter's default behaviors can be defined.
 * <p>
 * Assume, for example, our {@code fooService} is configured by default with a "remote first" invocation strategy -- i.e. attempt an invocation
 * against a remote endpoint and fall back to a local one should that remote attempt fail.  All direct invocations of {@code fooService.getFoo(...)}
 * by clients will exhibit this "remote first" strategy.  If, however, a client desired in a particular use case to override the default strategy to
 * forgo any attempt to target a remote endpoint, they could employ an {@code EndpointClient} thus:
 * <pre>
 *     {@code @Autowired} private EndpointClient endpointClient;
 *     ...
 *     FooResponse response = endpointClient.invoke(() -> fooService.getFoo(new FooRequest(...)))
 *         .usingLocalOnlyStrategy()
 *         .execute();
 * </pre>
 * <p>
 * The lambda-wrapped {@code fooService.getFoo(...)} call above will behave identically to a direct invocation of the same service in all ways
 * <strong>except</strong> for those overridden by the consumer prior to calling the {@code execute()} method on the endpoint client.
 * <p>
 * This is the least invasive means for customizing a service invocation's behavior, as it requires no accommodations at a service method or endpoint
 * method contract level.  An alternative but largely-equivalent approach, which requires a service method to return a specific invocation API but
 * obviates the need to broker that method's invocation through an {@code EndpointClient}, can be achieved by acquiring an {@link
 * EndpointInvocationHandle}.
 *
 * @author Jason Weiss
 * @see EndpointInvocationHandle
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EndpointClient {
    private final EndpointInvoker invoker;
    private final Map<String, IInvocationStrategy> strategies;

    /**
     * Identifies the service invocation to be performed once {@link Executable#execute() execute()} is called.
     *
     * @param <T> the type of response expected from the invocation
     * @param invocation the service invocation to be performed
     * @return an API for configuring the invocation
     */
    public <T> Configuring<T> invoke(Supplier<T> invocation) {
        return new Configuring<>(invocation);
    }

    /**
     * An interface exposing configuration options for a pending service invocation.
     *
     * @param <T> the type of response expected from the invocation
     */
    @RequiredArgsConstructor
    public class Configuring<T> {
        private final Supplier<T> invocation;
        private final EndpointInvocationContextBuilder invocationPreferences = EndpointInvocationContext.builder();

        /**
         * Overrides the configured or implied strategy for the pending service invocation.
         *
         * @param strategy the strategy to enforce when executing the pending service invocation
         * @return an API for executing the invocation
         */
        public Executable<T> using(InvocationStrategy strategy) {
            invocationPreferences.strategy(strategies.get(strategy.name()));
            return new Executable<>(invocation, invocationPreferences.build());
        }

        /**
         * Overrides the configured or implied strategy for this client's assigned invocation with the "local only" strategy.
         *
         * @return an API for executing the invocation
         */
        public Executable<T> usingLocalOnlyStrategy() {
            return using(LOCAL_ONLY);
        }

        /**
         * Overrides the configured or implied strategy for this client's assigned invocation with the "remote first" strategy.
         *
         * @return an API for executing the invocation
         */
        public Executable<T> usingRemoteFirstStrategy() {
            return using(REMOTE_FIRST);
        }

        /**
         * Overrides the configured or implied strategy for this client's assigned invocation with the "remote only" strategy.
         *
         * @return an API for executing the invocation
         */
        public Executable<T> usingRemoteOnlyStrategy() {
            return using(REMOTE_ONLY);
        }

        /**
         * Overrides the configured or implied strategy for this client's assigned invocation with the "simulated remote" strategy.
         *
         * @return an API for executing the invocation
         */
        public Executable<T> usingSimulatedRemoteStrategy() {
            return using(SIMULATED_REMOTE);
        }
    }

    @RequiredArgsConstructor
    public class Executable<T> {
        private final Supplier<T> invocation;
        private final EndpointInvocationContext invocationPreferences;

        public T execute() {
            return invoker.invokePreferred(invocation, invocationPreferences);
        }
    }
}

