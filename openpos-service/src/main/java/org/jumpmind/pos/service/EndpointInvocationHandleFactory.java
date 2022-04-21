package org.jumpmind.pos.service;

import org.jumpmind.pos.service.strategy.IInvocationStrategy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A factory which dispenses {@link EndpointInvocationHandle EndpointInvocationHandles} enabling a client to customize a derived and deterministic
 * {@link EndpointInvocationContext} before invoking a service method.
 *
 * @author Jason Weiss
 * @see EndpointInvocationHandle
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EndpointInvocationHandleFactory {
    private final EndpointInvoker endpointInvoker;
    private final Map<String, IInvocationStrategy> strategies;

    /**
     * Delivers an API allowing a client to customize the specified service invocation context before executing the corresponding service call.
     *
     * @param <T> the type of response expected from the service invocation
     * @param invocationContext the derived and deterministic invocation context for a given service method call
     * @return an API for overriding attributes of {@code invocationContext} before invoking the corresponding service call with the modified context
     */
    public <T> EndpointInvocationHandle<T> createServiceHandle(EndpointInvocationContext invocationContext) {
        return new EndpointInvocationHandle<>(endpointInvoker, strategies, invocationContext);
    }
}
