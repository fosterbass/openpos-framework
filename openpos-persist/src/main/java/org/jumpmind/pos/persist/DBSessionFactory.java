package org.jumpmind.pos.persist;

import java.io.InputStream;
import java.net.URL;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.db.model.Column;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.pos.persist.impl.*;
import org.jumpmind.pos.persist.model.*;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.jumpmind.properties.TypedProperties;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Slf4j
public class DBSessionFactory {

    DatabaseSchema databaseSchema;
    QueryTemplates queryTemplates;
    DmlTemplates dmlTemplates;
    IDatabasePlatform databasePlatform;
    TypedProperties sessionContext;
    @Getter
    List<Class<?>> modelClasses;
    @Getter
    List<Class<?>> modelExtensionClasses;
    TagHelper tagHelper;
    AugmenterHelper augmenterHelper;
    ClientContext clientContext;
    ShadowTablesConfigModel shadowTablesConfig;
    private ModelTagEnhancer modelTagEnhancer;
    private ModelAugmentEnhancer modelAugmentEnhancer;

    public void init(
            IDatabasePlatform databasePlatform,
            TypedProperties sessionContext,
            List<Class<?>> entities,
            List<Class<?>> extensionEntities,
            TagHelper tagHelper,
            AugmenterHelper augmenterHelper,
            ClientContext clientContext,
            ShadowTablesConfigModel shadowTablesConfig) {

        QueryTemplates queryTemplates = getQueryTemplates(sessionContext.get("module.tablePrefix"));
        DmlTemplates dmlTemplates = getDmlTemplates(sessionContext.get("module.tablePrefix"));

        init(databasePlatform, sessionContext, entities, extensionEntities, queryTemplates, dmlTemplates, tagHelper, augmenterHelper, clientContext, shadowTablesConfig);
    }

    public void init(
            IDatabasePlatform databasePlatform,
            TypedProperties sessionContext,
            List<Class<?>> entities,
            List<Class<?>> extensionEntities,
            QueryTemplates queryTemplatesObject,
            DmlTemplates dmlTemplates,
            TagHelper tagHelper,
            AugmenterHelper augmenterHelper,
            ClientContext clientContext,
            ShadowTablesConfigModel shadowTablesConfig) {

        this.queryTemplates = queryTemplatesObject;
        this.dmlTemplates = dmlTemplates;
        this.sessionContext = sessionContext;

        this.databasePlatform = databasePlatform;
        this.modelClasses = entities;
        this.modelExtensionClasses = extensionEntities;
        this.tagHelper = tagHelper;
        this.augmenterHelper = augmenterHelper;

        this.clientContext = clientContext;
        this.shadowTablesConfig = shadowTablesConfig;

        this.initSchema();

        modelTagEnhancer = new ModelTagEnhancer(databasePlatform, databaseSchema, tagHelper);
        modelAugmentEnhancer = new ModelAugmentEnhancer(databasePlatform, databaseSchema, augmenterHelper);

        enhanceSchema();
    }

    protected void enhanceSchema() {
        modelTagEnhancer.enhanceTaggedModels(this.modelClasses);
        modelAugmentEnhancer.augmentModels(this.modelClasses);
        databaseSchema.initPhase2();
    }

    protected void initSchema() {
        this.databaseSchema = new DatabaseSchema();
        databaseSchema.init(sessionContext.get("module.tablePrefix"), databasePlatform,
                this.modelClasses.stream().filter(e -> e.getAnnotation(org.jumpmind.pos.persist.TableDef.class) != null)
                        .collect(Collectors.toList()),
                this.modelExtensionClasses,
                this.augmenterHelper,
                this.clientContext,
                this.shadowTablesConfig,
                this.tagHelper
        );

        this.queryTemplates.replaceModelClassNamesWithTableNames(this.databaseSchema, this.modelClasses, (shadowTablesConfig != null) && shadowTablesConfig.validateTablesInQueries());
        this.dmlTemplates.replaceModelClassNamesWithTableNames(this.databaseSchema, this.modelClasses, (shadowTablesConfig != null) && shadowTablesConfig.validateTablesInQueries());
    }

    public void refreshModels() {
        databaseSchema.refreshModelMetaData();
    }

    public void createAndUpgrade() {
        databaseSchema.createAndUpgrade();
    }

    public List<Table> getTables(Class<?>... exclude) {
        List<Table> list = new ArrayList<>();
        List<Class<?>> toExclude = exclude != null ? Arrays.asList(exclude) : Collections.emptyList();
        for (Class<?> modelClazz : this.modelClasses) {
            if (!toExclude.contains(modelClazz)) {
                //  Note that the list below returns regular tables only, NOT shadow tables.
                List<Table> tables = this.databaseSchema.getTables("default", modelClazz);
                list.addAll(tables);
            }
        }
        return list;
    }

    public DBSession createDbSession() {
        return new DBSession(null, null, databaseSchema, databasePlatform, sessionContext, queryTemplates, dmlTemplates, tagHelper, augmenterHelper);
    }



    public static QueryTemplates getQueryTemplates(String tablePrefix) {
        final String resourceName = tablePrefix + "-query.yml";
        try {
            List<URL> urls = getQueryResources(resourceName);

            QueryTemplates templates = new QueryTemplates();

            for (URL url : urls) {
                log.info(String.format("Loading %s...", url.toString()));
                InputStream queryYamlStream = url.openStream();
                QueryTemplates queryTemplates = new Yaml(new Constructor(QueryTemplates.class)).load(queryYamlStream);
                if (queryTemplates != null) {
                    templates.addQueries("default", queryTemplates.getQueries());
                    templates.addQueries("training", queryTemplates.getQueries());
                }
            }
            
            return templates;
        } catch (Exception ex) {
            throw new PersistException("Failed to load " + resourceName, ex);
        }
    }

    public static DmlTemplates getDmlTemplates(String tablePrefix) {
        final String resourceName = tablePrefix + "-dml.yml";
        try {
            List<URL> urls = getQueryResources(resourceName);

            DmlTemplates templates = new DmlTemplates();

            for (URL url : urls) {
                log.info(String.format("Loading %s...", url.toString()));
                InputStream queryYamlStream = url.openStream();
                DmlTemplates dmlTemplates = new Yaml(new Constructor(DmlTemplates.class)).load(queryYamlStream);
                if (dmlTemplates != null) {
                    templates.addDmls("default", dmlTemplates.getDmls());
                    templates.addDmls("training", dmlTemplates.getDmls());
                }
            }

            return templates;
        } catch (Exception ex) {
            throw new PersistException("Failed to load " + resourceName, ex);
        }
    }

    protected static List<URL> getQueryResources(String resourceName) {
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(resourceName);

            if (!urls.hasMoreElements()) {
                log.debug("Could not locate " + resourceName + " on the classpath.");
                return new ArrayList<>();
            }

            List<URL> resources = Collections.list(urls);
            // reverse the order from classpath so that higher ordered files can applied last and thus take effect.
            // MMM 07-20-2021: Additional note, there probably isn't a strong reason the query yml's couldn't fit
            // under the application.yml structure at this point.
            Collections.reverse(resources);
            return resources;
        } catch (Exception ex) {
            throw new PersistException("Failed to load " + resourceName, ex);
        }
    }


}
