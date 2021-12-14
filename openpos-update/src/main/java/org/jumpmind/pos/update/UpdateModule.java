package org.jumpmind.pos.update;

import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.pos.persist.DBSession;
import org.jumpmind.pos.persist.DBSessionFactory;
import org.jumpmind.pos.service.AbstractRDBMSModule;
import org.jumpmind.pos.service.ModuleEnabledCondition;
import org.jumpmind.security.ISecurityService;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration("UpdateModule")
@EnableTransactionManagement()
@Conditional(ModuleEnabledCondition.class)
@Order(10)
public class UpdateModule extends AbstractRDBMSModule {

    public static final String NAME = "update";

    IUpdateService updateService;
    IUpdateManagementService updateManagementService;

    @Override
    protected String getArtifactName() {
        return "openpos-update";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTablePrefix() {
        return NAME;
    }
    @Override
    @Bean(name = NAME + "TxManager")
    public PlatformTransactionManager getPlatformTransactionManager() {
        return super.getPlatformTransactionManager();
    }

    @Override
    @Bean(name = NAME + "SecurityService")
    @Scope(value = "singleton", proxyMode = ScopedProxyMode.INTERFACES)
    protected ISecurityService securityService() {
        return super.securityService();
    }

    @Override
    @Bean(name = NAME + "SessionFactory")
    protected DBSessionFactory sessionFactory() {
        return super.sessionFactory();
    }

    @Override
    @Bean(name = NAME + "Session")
    public DBSession getDBSession() {
        return super.getDBSession();
    }

    @Override
    @Bean(name = NAME + "DatabasePlatform")
    public IDatabasePlatform getDatabasePlatform() {
        return super.getDatabasePlatform();
    }

    @Bean
    @Profile(UpdateModule.NAME)
    protected IUpdateService updateService() {
        if( updateService == null ){
            updateService = buildService(IUpdateService.class);
        }
        return updateService;
    }

    @Bean
    protected IUpdateManagementService updateManagementService() {
        if (updateManagementService == null) {
            updateManagementService = buildService(IUpdateManagementService.class);
        }

        return updateManagementService;
    }
}