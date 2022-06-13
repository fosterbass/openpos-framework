package org.jumpmind.pos.symds;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jumpmind.db.model.Table;
import org.jumpmind.pos.persist.DBSession;
import org.jumpmind.pos.persist.DBSessionFactory;
import org.jumpmind.pos.service.AbstractRDBMSModule;
import org.jumpmind.pos.service.ModuleEnabledCondition;
import org.jumpmind.security.ISecurityService;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.common.ParameterConstants;
import org.jumpmind.symmetric.io.data.Batch;
import org.jumpmind.symmetric.io.data.CsvData;
import org.jumpmind.symmetric.io.data.DataContext;
import org.jumpmind.symmetric.io.data.writer.DatabaseWriterFilterAdapter;
import org.jumpmind.symmetric.web.ServerSymmetricEngine;
import org.jumpmind.symmetric.web.SymmetricEngineHolder;
import org.jumpmind.symmetric.web.SymmetricServlet;
import org.jumpmind.symmetric.web.WebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.jumpmind.symmetric.common.Constants.*;

@Configuration("SymDSModule")
@EnableTransactionManagement
@Conditional(ModuleEnabledCondition.class)
@Order(20000)
@Slf4j
public class SymDSModule extends AbstractRDBMSModule {

    public final static String NAME = "sym";
    
    @Autowired
    ServletContext context;

    @Autowired
    CacheManager cacheManager;

    ServerSymmetricEngine serverEngine;

    @Value("${openpos.installationId:'not set'}")
    String installationId;

    @Autowired
    Environment env;

    @Autowired
    List<ISymDSConfigurator> configurators;

    @Autowired(required = false)
    List<IDataSyncListener> dataSyncListeners;

    @Autowired
    CacheEvictionConfig cacheEvictionConfig;

    @PostConstruct
    protected void setup() {
        SymmetricEngineHolder holder = new SymmetricEngineHolder();
        Properties properties = new Properties();
        configurators.forEach(c -> c.beforeCreate(properties));
        serverEngine = new ServerSymmetricEngine(getDataSource(), applicationContext, properties, false, holder);
        addConfiguredCacheEviction();
        holder.getEngines().put(properties.getProperty(ParameterConstants.EXTERNAL_ID), serverEngine);
        holder.setAutoStart(false);
        context.setAttribute(WebConstants.ATTR_ENGINE_HOLDER, holder);
        configurators.forEach(c -> c.beforeStart(serverEngine));
    }

    @Override
    public void initialize() {
        if ("true".equals(env.getProperty("openpos.symmetric.start", "false"))) {
            serverEngine.setup();
        } else {
            serverEngine.setupDatabase(false);
        }
        super.initialize();
    }

    private void addConfiguredCacheEviction() {
        serverEngine.getExtensionService().addExtensionPoint(new DatabaseWriterFilterAdapter() {
            @Override
            public void batchCommitted(DataContext context) {
                Batch batch = context.getBatch();
                try {
                    if (CHANNEL_RELOAD.equals(batch.getChannelId())) {
                        Collection<String> names = cacheManager.getCacheNames();
                        for (String name : names) {
                            cacheManager.getCache(name).clear();
                        }
                    } else {
                        if (cacheEvictionConfig != null) {
                            processCacheEvictionForTables(context);
                            processCacheEvictionForChannels(context);
                        }
                    }
                } catch (Exception e) {
                    //we never want to hold up sync due to any issues with cache expiration
                    log.warn("Error clearing cache on SymDS sync",e);
                }
            }

            @Override
            public void afterWrite(DataContext context, Table table, CsvData data) {
                processDataSyncListeners(context, table, data);
            }
        });
    }

    protected void processCacheEvictionForTables(DataContext context) throws Exception {
        Map<String, List<String>> tables = cacheEvictionConfig.getTables();
        if (tables != null) {
            Map<String, Table> batchTables = context.getParsedTables();
            for (Map.Entry<String, Table> entry : batchTables.entrySet()) {
                if (entry.getValue() != null) {
                    processCacheEvictions(tables.get(entry.getValue().getName()));
                }
            }
        } else {
            log.info("No tables defined for cacheEvictionConfig");
        }
    }

    protected void processCacheEvictionForChannels(DataContext context) throws Exception {
        Map<String, List<String>> channels = cacheEvictionConfig.getChannels();
        if (channels != null) {
            String batchChannel = context.getBatch().getChannelId();
            processCacheEvictions(channels.get(batchChannel));
        } else {
            log.info("No channels defined for cacheEvictionConfig");
        }
    }

    protected void processCacheEvictions(List<String> evictionCacheNames) throws Exception {
        if (evictionCacheNames != null && !evictionCacheNames.isEmpty()) {
            for (String evictionCacheName:evictionCacheNames) {
                Cache cache = cacheManager.getCache(evictionCacheName);
                if (cache != null) {
                    log.info(String.format("Clearing cache %s", evictionCacheName));
                    cache.clear();
                } else {
                    log.warn(String.format("Configured cache %s not found in active caches", evictionCacheName));
                }
            }
        }
    }

    protected void processDataSyncListeners(DataContext context, Table table, CsvData data) {
        if (CollectionUtils.isEmpty(dataSyncListeners)
                || table == null
                || table.getName() == null) {
            return;
        }

        String channelId = context.getBatch().getChannelId();
        String tableName = table.getName().toUpperCase();
        SyncData syncData = null;

        for (IDataSyncListener dataSyncListener : dataSyncListeners) {
            if (dataSyncListener.isApplicable(channelId, tableName)) {
                if (syncData == null) {
                    syncData = buildSyncData(tableName, context, table, data);
                }
                dataSyncListener.onDataWrite(syncData);
            }
        }
    }

    protected SyncData buildSyncData(String tableName, DataContext context, Table table, CsvData data) {
        SyncData syncData = new SyncData();
        syncData.setChannelId(context.getBatch().getChannelId());
        syncData.setTableName(tableName);
        syncData.setDataEventType(data.getDataEventType());

        Map<String, String> mappedRowData = new LinkedHashMap<>();
        String[] rowData = data.getParsedData("rowData");
        if (table.getColumnNames() != null && rowData != null) {
            for (String columnName : table.getColumnNames()) {
                int index = table.getColumnIndex(columnName);
                if (index >= 0 && index < rowData.length) {
                    mappedRowData.put(columnName.toUpperCase(), rowData[index]);
                }
            }
        }

        syncData.setData(mappedRowData);
        return syncData;
    }

    @Override
    public void start() {
        if ("true".equals(env.getProperty("openpos.symmetric.start", "false"))) {
            serverEngine.start();
        }
        super.start();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected String getArtifactName() {
        return NAME;
    }

    @Override
    public String getTablePrefix() {
        return NAME;
    }

    @Bean
    public ServletRegistrationBean<SymmetricServlet> symServlet() {
        ServletRegistrationBean<SymmetricServlet> bean = new ServletRegistrationBean<>(new SymmetricServlet(), configurators.get(configurators.size()-1).getWebContext());
        bean.setLoadOnStartup(1);
        return bean;
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

    @Bean
    ISymmetricEngine symmetricEngine() {
        return this.serverEngine;
    }
}
