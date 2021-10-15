package org.jumpmind.pos.util.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Provides access to the Spring ApplicationContext for beans not
 * managed by Spring. Should be used sparingly.
 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext appContext) {
        ctx = appContext;
    }

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }
}