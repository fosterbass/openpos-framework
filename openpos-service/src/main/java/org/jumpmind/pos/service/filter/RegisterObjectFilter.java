package org.jumpmind.pos.service.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RegisterObjectFilter {

    @Autowired(required = false) // not required for other tests that load this.
    RequestMappingHandlerAdapter adapter;
    @Autowired
    OpenposHttpObjectFilter openposHttpObjectFilter;

    @Value("${openpos.general.backwardCompatibility.enabled:true}")
    boolean backwardCompatibilityEnabled;
    
    @PostConstruct
    public void prioritizeCustomArgumentMethodHandlers() {
        if (adapter != null && backwardCompatibilityEnabled) {
            List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(adapter.getArgumentResolvers());
            argumentResolvers.add(0, openposHttpObjectFilter);
            adapter.setArgumentResolvers(argumentResolvers);
        }
    }
}