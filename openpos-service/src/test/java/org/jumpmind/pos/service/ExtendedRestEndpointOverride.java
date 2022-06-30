package org.jumpmind.pos.service;

import org.jumpmind.pos.util.RestApiSupport;

@EndpointOverride(path = RestApiSupport.REST_API_CONTEXT_PATH + "/base/endpointWithRestPrefixInPath")
public class ExtendedRestEndpointOverride extends BaseRestTestEndpoint {

    public int invokeCount = 0;

    @Override
    public void test() {
        invokeCount++;
    }
}
