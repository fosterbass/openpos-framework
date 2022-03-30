package org.jumpmind.pos.persist.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.db.model.Column;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.pos.persist.Tagged;
import org.jumpmind.pos.persist.model.ITaggedModel;
import org.jumpmind.pos.persist.model.TagHelper;
import org.jumpmind.pos.persist.model.TagModel;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;

import static org.jumpmind.pos.persist.impl.DatabaseSchema.DEFAULT_COLUMN_SIZE;

@Slf4j
@Data
@AllArgsConstructor
public class ModelTagEnhancer {

    private IDatabasePlatform databasePlatform;
    private DatabaseSchema databaseSchema;
    private TagHelper tagHelper;
    
    public void enhanceTaggedModels(List<Class<?>> modelClasses) {
        if (tagHelper != null) {
            List<TagModel> tags = tagHelper.getTagConfig().getTags();
            for (Class<?> modelClass : modelClasses) {
                Tagged[] annotations = modelClass.getAnnotationsByType(Tagged.class);
                if (annotations.length > 0 || ITaggedModel.class.isAssignableFrom(modelClass)) {
                    boolean includeTagsInPrimaryKey = true;
                    if (annotations.length > 0) {
                        includeTagsInPrimaryKey = annotations[0].includeTagsInPrimaryKey();
                    }
                    enhanceTaggedTable(modelClass, tags, includeTagsInPrimaryKey);
                }
            }
        }
    }

    protected void enhanceTaggedTable(Class<?> entityClass, List<TagModel> tags, boolean includeInPk) {
        //  Normal table.
        Table table = databaseSchema.getTableForDeviceMode("default", entityClass);
        warnOrphanedTagColumns(tags, table);
        modifyTagColumns(tags, table, includeInPk);
        addTagColumns(tags, table, includeInPk);

        //  The corresponding shadow table, if any.
        Table shadowTable = databaseSchema.getTableForDeviceMode("training", entityClass);
        if ((shadowTable != null) && !shadowTable.getName().equalsIgnoreCase(table.getName()))  {
            warnOrphanedTagColumns(tags, shadowTable);
            modifyTagColumns(tags, shadowTable, includeInPk);
            addTagColumns(tags, shadowTable, includeInPk);
        }
    }

    protected void modifyTagColumns(List<TagModel> tags, Table table, boolean includeInPk) {
        for (Column existingColumn : table.getColumns()) {
            for (TagModel tag : tags) {
                if (StringUtils.equalsIgnoreCase(getColumnName(tag), existingColumn.getName())) {
                    setColumnInfo(existingColumn, table, tag, includeInPk);
                    break;
                }
            }
        }
    }

    protected void addTagColumns(List<TagModel> tags, Table table, boolean modifyPk) {
        for (TagModel tag : tags) {
            if (table.getColumnIndex(getColumnName(tag)) == -1) {
                Column tagColumn = generateTagColumn(tag, table, modifyPk);
                table.addColumn(tagColumn);
            }
        }
    }

    protected void warnOrphanedTagColumns(List<TagModel> tags, Table table) {
        for (Column existingColumn : table.getColumns()) {
            if (!existingColumn.getName().toUpperCase().startsWith(TagModel.TAG_PREFIX)) {
                continue;
            }

            boolean matched = false;
            for (TagModel tag : tags) {
                if (StringUtils.equalsIgnoreCase(getColumnName(tag), existingColumn.getName())) {
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

    protected Column generateTagColumn(TagModel tag, Table table, boolean modifyPk) {
        return setColumnInfo(new Column(), table, tag, modifyPk);
    }

    protected String getColumnName(TagModel tag) {
        return databasePlatform.alterCaseToMatchDatabaseDefaultCase(TagModel.TAG_PREFIX + tag.getName().toUpperCase());
    }

    protected Column setColumnInfo(Column column, Table table, TagModel tag, boolean includeInPk) {
        column.setName(getColumnName(tag));
        column.setPrimaryKey(includeInPk);
        if (includeInPk) {
            column.setPrimaryKeySequence(table.getPrimaryKeyColumnCount() + 1);
        }
        column.setRequired(true);
        column.setDefaultValue(TagModel.TAG_ALL);
        column.setTypeCode(Types.VARCHAR);
        if (tag.getSize() > 0) {
            column.setSize(String.valueOf(tag.getSize()));
        } else {
            column.setSize(DEFAULT_COLUMN_SIZE);
        }
        return column;
    }

}
