package org.jumpmind.pos.server.status;

import org.jumpmind.pos.server.status.service.IServerStatusService;
import org.jumpmind.pos.service.AbstractServiceFactory;
import org.jumpmind.pos.service.init.IModuleStatusProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration("ServerStatusModule")
public class ServerStatusModule extends AbstractServiceFactory {

    @Autowired(required = false)
    List<IModuleStatusProvider> initProviders;

    @Bean
    public IServerStatusService serverStatusService() {
        return buildService(IServerStatusService.class);
    }

    @Bean
    public FilterRegistrationBean<RejectUntilInitComplete> initServerLoadedFilter() {
        if (initProviders == null) {
            initProviders = Collections.emptyList();
        }

        final FilterRegistrationBean<RejectUntilInitComplete> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RejectUntilInitComplete(initProviders));
        registration.addUrlPatterns("*");

        return registration;
    }
}
