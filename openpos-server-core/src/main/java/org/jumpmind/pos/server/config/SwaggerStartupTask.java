package org.jumpmind.pos.server.config;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.util.startup.AbstractStartupTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;

import static java.lang.Integer.MAX_VALUE;

@Slf4j
@Component
@Order(MAX_VALUE-100)
public class SwaggerStartupTask extends AbstractStartupTask {

    @Autowired
    DocumentationPluginsBootstrapper bootstrapper;

    @Value("${springfox.documentation.auto-startup:true}")
    boolean swaggerAutoStart;

    @Override
    protected void doTask() throws Exception {
        if (!swaggerAutoStart) {
            new Thread(() -> {
                log.info("Reading in swagger service definitions");
                bootstrapper.start();
                log.info("Done reading in swagger service definitions");
            }, "swagger-startup-thread").start();
        }
    }

}
