package org.jumpmind.pos.service.compatibility;

import org.jumpmind.pos.service.EndpointInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Proxy;

@Configuration
@ComponentScan(
        basePackages = {  "org.jumpmind.pos.service", "org.jumpmind.pos.util" })
public class TestingCompatibilityModule {

    @Autowired
    protected EndpointInvoker dispatcher;

    @Bean
    ITestingCustomerService testingCustomerService() {
        return (ITestingCustomerService) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{ITestingCustomerService.class},
                dispatcher);
    }
}