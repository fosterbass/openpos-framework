package org.jumpmind.pos.service;

import org.jumpmind.pos.service.strategy.InvocationStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestServiceConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndpointOverrideTests {

    @Autowired
    private EndpointInvocationHandler dispatcher;

    @Autowired
    @Qualifier("baseTestEndpoint")
    private BaseTestEndpoint baseEndpointToOverrideWithSpringCGLibProxiedEndpoint;
    @Autowired
    private SpringCGLibProxiedEndpointOverride springCGLibProxiedEndpointOverride;


    @Autowired
    @Qualifier("baseRestTestEndpoint")
    private BaseRestTestEndpoint baseEndpointWithRestInPath;
    @Autowired
    private ExtendedRestEndpointOverride endpointOverrideWithRestInPath;


    @Autowired
    ServiceConfig config;

    @Before
    public void setupServiceConfig() {
        Map<String, ServiceSpecificConfig> specificConfigMap = new HashMap<>();
        ServiceSpecificConfig specificConfig = new ServiceSpecificConfig();
        // Make default strategy remote so it will fail if the override does not work and the invoker it falls back to base endpoint
        specificConfig.setStrategy(InvocationStrategy.REMOTE_ONLY);

        List<EndpointSpecificConfig> endpointConfigs = new ArrayList<>();
        // For ensure_configured_endpoint_override_that_is_wrapped_by_spring_CGLib_proxy_is_invoked()
        EndpointSpecificConfig endpointCfg = new EndpointSpecificConfig();
        endpointCfg.setPath("/base/testProxiedEndpoint");
        endpointCfg.setStrategy(InvocationStrategy.LOCAL_ONLY);
        endpointCfg.setProfile("local");
        endpointConfigs.add(endpointCfg);


        // For ensure_config_path_without_rest_prefix_still_is_matched_to_endpoint_with_rest_prefix()
        endpointCfg = new EndpointSpecificConfig();
        // Purposefully omit the "rest" prefix from the path to check that the endpoint is still resolved
        endpointCfg.setPath("/base/endpointWithRestPrefixInPath");
        endpointCfg.setStrategy(InvocationStrategy.LOCAL_ONLY);
        endpointCfg.setProfile("local");
        endpointConfigs.add(endpointCfg);

        specificConfig.setEndpoints(endpointConfigs);
        specificConfigMap.put("override_test", specificConfig);

        config.setSpecificConfig(specificConfigMap);
    }

    /**
     * This test fails without changes made in PR #2338 and covers those changes.
     * @throws Throwable
     */
    @Test
    public void ensure_configured_endpoint_override_that_is_wrapped_by_spring_CGLib_proxy_is_invoked() throws Throwable {
        assertEquals(0, baseEndpointToOverrideWithSpringCGLibProxiedEndpoint.invokeCount);
        assertEquals(0, springCGLibProxiedEndpointOverride.invokeCount);
        dispatcher.invoke(null, TestServiceConfig.ProxyForProxiedOverrideTest.class.getMethod("test"), null);
        assertEquals(0, baseEndpointToOverrideWithSpringCGLibProxiedEndpoint.invokeCount);
        assertEquals(1, springCGLibProxiedEndpointOverride.invokeCount);
    }

    /**
     * This test fails without changes made in PR #2338 and covers those changes.
     * @throws Throwable
     */
    @Test
    public void ensure_config_path_without_rest_prefix_still_is_matched_to_endpoint_with_rest_prefix() throws Throwable {
        assertEquals(0, baseEndpointWithRestInPath.invokeCount);
        assertEquals(0, endpointOverrideWithRestInPath.invokeCount);
        dispatcher.invoke(null, TestServiceConfig.ProxyForOverrideWithRestInPathTest.class.getMethod("test"), null);
        assertEquals(0, baseEndpointWithRestInPath.invokeCount);
        assertEquals(1, endpointOverrideWithRestInPath.invokeCount);
    }

}
