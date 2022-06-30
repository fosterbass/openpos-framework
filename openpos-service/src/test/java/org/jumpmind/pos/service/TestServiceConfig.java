package org.jumpmind.pos.service;

import org.jumpmind.pos.util.RestApiSupport;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@ComponentScan(
        basePackages = {"org.jumpmind.pos.service", "org.jumpmind.pos.util"})
@EnableConfigurationProperties
@SpringBootApplication
public class TestServiceConfig {


    /**
     * Used for {@link EndpointOverrideTests#ensure_config_path_without_rest_prefix_still_is_matched_to_endpoint_with_rest_prefix()}
     */
    @Bean
    IOverrideWithRestInPathTest overrideWithRestTest() {
        return new ProxyForOverrideWithRestInPathTest();
    }

    /**
     * Used for {@link EndpointOverrideTests#ensure_configured_endpoint_override_that_is_wrapped_by_spring_CGLib_proxy_is_invoked()}
     */
    @Bean
    IProxiedTestService overrideWithProxyTest() {
        return new ProxyForProxiedOverrideTest();
    }

    @Bean
    ITest test() {
        return new Proxy();
    }

    @RestController("test")
    @RequestMapping("/this/is/a/test")
    interface ITest {
        void test();
    }

    @RestController("override_test")
    @RequestMapping(RestApiSupport.REST_API_CONTEXT_PATH + "/base/endpointWithRestPrefixInPath")
    interface IOverrideWithRestInPathTest {
        void test();
    }

    @RestController("override_test")
    @RequestMapping("/base/testProxiedEndpoint")
    interface IProxiedTestService {
        void test();
    }

    public class Proxy implements ITest {
        @Override
        public void test() {
        }
    }

    /**
     * Used for {@link EndpointOverrideTests#ensure_config_path_without_rest_prefix_still_is_matched_to_endpoint_with_rest_prefix()}
     */
    public class ProxyForOverrideWithRestInPathTest implements IOverrideWithRestInPathTest {
        @Override
        public void test() {
        }
    }

    /**
     * Used for {@link EndpointOverrideTests#ensure_configured_endpoint_override_that_is_wrapped_by_spring_CGLib_proxy_is_invoked()}
     */
    public class ProxyForProxiedOverrideTest implements IProxiedTestService {
        @Override
        public void test() {
        }
    }
}
