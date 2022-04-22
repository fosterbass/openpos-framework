package org.jumpmind.pos.service;

import org.jumpmind.pos.service.strategy.IInvocationStrategy;
import org.jumpmind.pos.service.strategy.InvocationStrategy;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

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
 * and method-annotated instructions.  The invoking client has no control over those behaviors, as each invocation should behave, at a domain-agnostic
 * level, identically to every other of its peer invocations.
 * <p>
 * But occasionally the client caller requires the ability to change these statically-defined behaviors at the point of invocation.  Assume, for
 * example, our {@code fooService} is configured by default with a "remote first" invocation strategy -- i.e. attempt an invocation against a remote
 * endpoint and fall back to a local one should that remote attempt fail.  And also assume a client in a particular use case needs to override the
 * default strategy to forgo any attempt to target a remote endpoint.
 * <p>
 * Our existing service method contract does not permit this degree of situational control.  Consider, however, if we modified our service method's
 * signature to be:
 * <pre>
 *     EndpointInvocationHandle&lt;FooResponse&gt; getFoo(FooRequest request);
 * </pre>
 * <p>
 * We would then be able to satisfy our "in this case, use the local-only invocation strategy" requirement by changing our client code to:
 * <pre>
 *     FooResponse response = fooService.getFoo(new FooRequest(...))
 *         .usingLocalOnlyStrategy()
 *         .execute();
 * </pre>
 * <p>
 * Obviously, the manner in which the client-customized invocation problem is solved by {@code EndpointInvocationHandle} is "invasive" by way of its
 * requiring changes to the service method's return type.  As such, this solution should probably be avoided unless implemented as part of a
 * system-wide change to our service methods; i.e. if we were to require <strong>every</strong> service method to have a return type of
 * {@code EndpointInvocationHandler}.
 * <p>
 * Since such a universal change is probably impractical and not even necessarily desirable (a lot of JMC's proxy-driven endpoint invocation magic
 * minimizes the advantages of enforcing formal contracts on our service methods), the same problems addressed by {@code EndpointInvocationHandler}
 * can be addressed just as well by wrapping an existing, contractually-unbound service method call in a lambda expression supplied to an
 * {@link EndpointClient}.
 * <p>
 * In other words, you made it all the way to the end of this novella just to find out you should have been reading another one.  It's only human to
 * feel cheated.
 *
 * @param <T> the type of response expected from a service method invocation
 * @author Jason Weiss
 * @see EndpointClient
 */
@RequiredArgsConstructor
public class EndpointInvocationHandle<T> {
    private final EndpointInvoker endpointInvoker;
    private final Map<String, IInvocationStrategy> strategies;
    private final EndpointInvocationContext invocationContext;

    /**
     * @see #execute()
     */
    public T $() {
        return execute();
    }

    /**
     * Invokes the previously-configured endpoint operation.
     *
     * @return the result of invoking this client's assigned endpoint operation
     */
    @SuppressWarnings("unchecked")
    public T execute() {
        try {
            return (T) endpointInvoker.invoke(invocationContext);
        }
        catch (Throwable ex) {
            ExceptionUtils.rethrow(ex);
            return null;
        }
    }

    /**
     * Overrides the configured or implied strategy for this client's assigned invocation.
     *
     * @param strategy the strategy to enforce when invoking this client's assigned invocation
     * @return this client
     */
    public EndpointInvocationHandle<T> using(InvocationStrategy strategy) {
        invocationContext.setStrategy(strategies.get(strategy.name()));
        return this;
    }

    /**
     * Overrides the configured or implied strategy for this client's assigned invocation with the "local only" strategy.
     *
     * @return this client
     */
    public EndpointInvocationHandle<T> usingLocalOnlyStrategy() {
        return using(LOCAL_ONLY);
    }

    /**
     * Overrides the configured or implied strategy for this client's assigned invocation with the "remote first" strategy.
     *
     * @return this client
     */
    public EndpointInvocationHandle<T> usingRemoteFirstStrategy() {
        return using(REMOTE_FIRST);
    }

    /**
     * Overrides the configured or implied strategy for this client's assigned invocation with the "remote only" strategy.
     *
     * @return this client
     */
    public EndpointInvocationHandle<T> usingRemoteOnlyStrategy() {
        return using(REMOTE_ONLY);
    }

    /**
     * Overrides the configured or implied strategy for this client's assigned invocation with the "simulated remote" strategy.
     *
     * @return this client
     */
    public EndpointInvocationHandle<T> usingSimulatedRemoteStrategy() {
        return using(SIMULATED_REMOTE);
    }
}
