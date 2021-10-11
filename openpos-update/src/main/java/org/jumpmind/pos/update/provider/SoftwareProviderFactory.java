package org.jumpmind.pos.update.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SoftwareProviderFactory {

    @Value("${openpos.update.softwareProvider:fileSystemSoftwareProvider}")
    String softwareProvider;

    @Autowired
    ApplicationContext applicationContext;
    
    public ISoftwareProvider getSoftwareProvider() {
        log.info("Using " + softwareProvider);
        return applicationContext.getBean(softwareProvider, ISoftwareProvider.class);
    }
    
}
