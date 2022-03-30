package org.jumpmind.pos.persist.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.db.model.Column;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.pos.persist.Augmented;
import org.jumpmind.pos.persist.model.*;

import java.sql.Types;
import java.util.List;

import static org.jumpmind.pos.persist.impl.DatabaseSchema.DEFAULT_COLUMN_SIZE;

@Slf4j
@Data
@AllArgsConstructor
public class ModelAugmentEnhancer {

    private IDatabasePlatform databasePlatform;
    private DatabaseSchema databaseSchema;
    private AugmenterHelper augmenterHelper;

    public void augmentModels(List<Class<?>> modelClasses) {
        if (augmenterHelper != null) {
            AugmenterConfigs augmenterConfigs = augmenterHelper.getAugmenterConfigs();

            for (Class<?> clazz : modelClasses) {
                Augmented[] annotations = clazz.getAnnotationsByType(Augmented.class);
                if (annotations.length > 0) {
                    AugmenterConfig augmenterConfig = augmenterConfigs.getConfigByName(annotations[0].name());
                    if (augmenterConfig != null) {
                        augmentTable(clazz, augmenterConfig);
                    }
                    else {
                        log.info("Missing augmenter name " + annotations[0].name() + " defined in augmenter configuration");
                    }
                }
            }
        }
    }

    protected void augmentTable(Class<?> entityClass, AugmenterConfig augmenterConfig) {
        //  Normal table.
        Table table = databaseSchema.getTableForDeviceMode("default", entityClass);
        warnOrphanedAugmentedColumns(augmenterConfig, table);
        modifyAugmentColumns(augmenterConfig, table);
        addAugmentColumns(augmenterConfig, table);

        //  The corresponding shadow table, if any.
        Table shadowTable = databaseSchema.getTableForDeviceMode("training", entityClass);
        if ((shadowTable != null) && !shadowTable.getName().equalsIgnoreCase(table.getName())) {
            warnOrphanedAugmentedColumns(augmenterConfig, shadowTable);
            modifyAugmentColumns(augmenterConfig, shadowTable);
            addAugmentColumns(augmenterConfig, shadowTable);
        }
    }

    protected void addAugmentColumns(AugmenterConfig augmenterConfig, Table table) {
        for (AugmenterModel augmenter : augmenterConfig.getAugmenters()) {
            if (table.getColumnIndex(getColumnName(augmenterConfig.getPrefix(), augmenter)) == -1) {
                Column tagColumn = generateAugmentColumn(augmenterConfig, augmenter);
                table.addColumn(tagColumn);
            }
        }
    }

    protected Column generateAugmentColumn(AugmenterConfig augmenterConfig, AugmenterModel augmenter) {
        return setColumnInfo(new Column(), augmenterConfig.getPrefix(), augmenter);
    }

    protected void modifyAugmentColumns(AugmenterConfig augmenterConfig, Table table) {
        for (Column existingColumn : table.getColumns()) {
            for (AugmenterModel augmenter : augmenterConfig.getAugmenters()) {
                if (StringUtils.equalsIgnoreCase(getColumnName(augmenterConfig.getPrefix(), augmenter), existingColumn.getName())) {
                    setColumnInfo(existingColumn, augmenterConfig.getPrefix(), augmenter);
                    break;
                }
            }
        }
    }

    protected Column setColumnInfo(Column column, String prefix, AugmenterModel augmenter) {
        column.setName(getColumnName(prefix, augmenter));
        column.setDefaultValue(augmenter.getDefaultValue());
        column.setTypeCode(Types.VARCHAR);
        if (augmenter.getSize() != null && augmenter.getSize() > 0) {
            column.setSize(String.valueOf(augmenter.getSize()));
        } else {
            column.setSize(DEFAULT_COLUMN_SIZE);
        }
        return column;
    }

    private void warnOrphanedAugmentedColumns(AugmenterConfig augmenterConfig, Table table) {
        for (Column existingColumn : table.getColumns()) {
            if (!existingColumn.getName().toUpperCase().startsWith(augmenterConfig.getPrefix())) {
                continue;
            }

            boolean matched = false;
            for (AugmenterModel augmenter : augmenterConfig.getAugmenters()) {
                if (StringUtils.equalsIgnoreCase(getColumnName(augmenterConfig.getPrefix(), augmenter), existingColumn.getName())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                log.info("Orphaned tag column detected.  This column should be manually dropped if no longer needed: " + table + " "
                        + existingColumn);
            }
        }
    }

    protected String getColumnName(String prefix, AugmenterModel augmenter) {
        return databasePlatform.alterCaseToMatchDatabaseDefaultCase(prefix + augmenter.getName().toUpperCase());
    }
}
