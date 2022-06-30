package org.jumpmind.pos.service;

import org.jumpmind.pos.util.RestApiSupport;

/**
 * This base endpoint is specifically here to be overridden by {@link ExtendedRestEndpointOverride}
 * for the tests in EndpointOverrideTests
 */
@Endpoint(path=RestApiSupport.REST_API_CONTEXT_PATH + "/base/endpointWithRestPrefixInPath")
public class BaseRestTestEndpoint {

    public int invokeCount = 0;

    public void test() {
        invokeCount++;
    }
}
