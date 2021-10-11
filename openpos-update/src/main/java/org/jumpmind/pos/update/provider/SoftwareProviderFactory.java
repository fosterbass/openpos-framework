package org.jumpmind.pos.update.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SoftwareProviderFactory {

    @Value("${openpos.update.softwareProvider:fileSystemSoftwareProvider}")
    String softwareProvider;

    @Autowired
    ApplicationContext applicationContext;
    
    public ISoftwareProvider getSoftwareProvider() {
        return applicationContext.getBean(softwareProvider, ISoftwareProvider.class);
    }
    
}
