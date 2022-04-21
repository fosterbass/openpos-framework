package org.jumpmind.pos.service;

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

    @Bean
    ITest test() {
        return new Proxy();
    }

    @RestController("test")
    @RequestMapping("/this/is/a/test")
    interface ITest {
        public void test();
    }

    public class Proxy implements ITest {
        @Override
        public void test() {
        }
    }

}
