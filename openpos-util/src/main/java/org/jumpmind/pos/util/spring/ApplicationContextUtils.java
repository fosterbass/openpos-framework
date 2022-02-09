package org.jumpmind.pos.util.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Provides access to the Spring ApplicationContext for beans not
 * managed by Spring. Should be used sparingly.
 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    private ApplicationContext ctx;
    private static ApplicationContextUtils instance;

    @PostConstruct
    void init() {
        instance = this;
    }

    public static ApplicationContextUtils instance() {
        return instance;
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) {
        this.ctx = appContext;
    }

    public ApplicationContext getApplicationContext() {
        return ctx;
    }
}