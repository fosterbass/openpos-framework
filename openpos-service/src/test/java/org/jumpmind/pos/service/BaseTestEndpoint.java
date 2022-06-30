package org.jumpmind.pos.service;

/**
 * This base endpoint is specifically here to be overridden by {@link SpringCGLibProxiedEndpointOverride}
 * for the tests in EndpointOverrideTests
 */
@Endpoint(path="/base/testProxiedEndpoint")
public class BaseTestEndpoint {

    public int invokeCount = 0;

    public void test() {
        invokeCount++;
    }
}
