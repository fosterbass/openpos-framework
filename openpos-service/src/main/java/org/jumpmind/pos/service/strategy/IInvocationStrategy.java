package org.jumpmind.pos.service.strategy;

import org.jumpmind.pos.service.EndpointInvocationContext;

public interface IInvocationStrategy {
    Object invoke(EndpointInvocationContext endpointInvocationContext) throws Throwable;

    String getStrategyName();
}
