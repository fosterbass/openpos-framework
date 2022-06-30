package org.jumpmind.pos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@EndpointOverride(path = "/base/testProxiedEndpoint")
public class SpringCGLibProxiedEndpointOverride extends BaseTestEndpoint {

    // Making static because spring creates CGLIB wrapped object which is different
    // from the bean injected to the test
    public static int invokeCount = 0;

    // Will cause spring to make this a CGLIB wrapped endpoint
    @Transactional
    void foo() {
    }

    @Override
    public void test() {
        invokeCount++;
    }
}
