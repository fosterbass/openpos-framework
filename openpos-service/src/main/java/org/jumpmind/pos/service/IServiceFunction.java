package org.jumpmind.pos.service;

/**
 * A standardized, first-class contract for functions operating in service of business functions implemented by {@link Endpoint Endpoints}.
 *
 * @author Jason Weiss
 * @see Endpoint
 */
@FunctionalInterface
public interface IServiceFunction<I, O> {
    /**
     * Supplies a request to this service function and produces a response.
     *
     * @param request the request to consume
     * @return the result of satisfying {@code request}
     */
    O invoke(I request);
}
