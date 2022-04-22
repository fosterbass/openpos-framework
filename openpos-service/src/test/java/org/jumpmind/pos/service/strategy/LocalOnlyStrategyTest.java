package org.jumpmind.pos.service.strategy;

import org.jumpmind.pos.service.EndpointInvocationHandler;
import org.jumpmind.pos.service.TestEndpoint;
import org.jumpmind.pos.service.TestEndpointOverride;
import org.jumpmind.pos.service.TestServiceConfig;
import org.jumpmind.pos.service.TestServiceConfig.Proxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestServiceConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocalOnlyStrategyTest {

    @Autowired
    private EndpointInvocationHandler dispatcher;

    @Autowired
    private TestEndpoint endpoint;

    @Autowired
    private TestEndpointOverride override;

    @Test
    public void testThatOverrideIsCalled() throws Throwable {
        assertEquals(0, endpoint.invokeCount);
        assertEquals(0, override.invokeCount);
        dispatcher.invoke(null, Proxy.class.getMethod("test"), null);
        assertEquals(0, endpoint.invokeCount);
        assertEquals(1, override.invokeCount);
    }
}
