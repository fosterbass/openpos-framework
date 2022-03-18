package org.jumpmind.pos.persist.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.joda.money.Money;
import org.jumpmind.db.model.Column;
import org.jumpmind.pos.persist.Augmented;
import org.jumpmind.pos.persist.model.*;
import org.jumpmind.pos.util.model.ITypeCode;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

@Data
@Slf4j
public class ModelMetaData {

    private List<ModelClassMetaData> modelClassMetaData;
    private Field systemDataField;
    PropertyDescriptor[] propertyDescriptors;
    Set<String> deferredLoadFieldNames = new HashSet<>();
    Set<String> moneyFieldNames = new HashSet<>();
    Set<String> typeCodeFieldNames = new HashSet<>();
    Map<String, String> propertyToColumnNames = new HashMap<>();
    String[] fieldOrder;
    private Map<String, Column> fieldsToColumns = new LinkedHashMap<>();
    private AugmenterHelper augmenterHelper;
    Map<Class<?>, PropertyDescriptor[]> exentionClassToPropertyDescriptors;

    public void init() {
        systemDataField = FieldUtils.getField(modelClassMetaData.get(modelClassMetaData.size()-1).getModelClass(), "systemData", true);
        if (CollectionUtils.isNotEmpty(modelClassMetaData)) {
            Class<?> modelClass = modelClassMetaData.get(0).getModelClass();
            propertyDescriptors = PropertyUtils.getPropertyDescriptors(modelClass);
        }
    }

    public void initPhase2() {
        Class<?> modelClass = modelClassMetaData.get(0).getModelClass();
        initOptimizations();
        fieldsToColumns = mapFieldsToColumns(modelClass);
        initExentionClassToPropertyDescriptors();
    }

    public PropertyDescriptor[] getPropertyDescriptorsForExtension(Class<?> extensionClass) {
        return exentionClassToPropertyDescriptors.get(extensionClass);
    }

    private void initExentionClassToPropertyDescriptors() {
        exentionClassToPropertyDescriptors = new HashMap<>();
        modelClassMetaData.forEach(modelClassMeta ->
                exentionClassToPropertyDescriptors.putAll(modelClassMeta.getExentionClassToPropertyDescriptors()));
    }

    /**
     * Returns either the column name associated with the property on the given class or the column name of the first
     * matching property found in superclasses.  The column name associated with the property on the given class
     * has highest priority.
     * @param propertyName The bean property name to search for
     * @return The column name associated with the given property.
     */
    public <T> String getColumnNameForProperty(String propertyName) {
        return propertyToColumnNames.get(propertyName);
    }

    @Override
    public String toString() {
        return modelClassMetaData.toString();
    }

    public boolean isDeferredLoadField(String propertyName) {
        return deferredLoadFieldNames.contains(propertyName);
    }

    public boolean isMoneyField(String propertyName) {
        return moneyFieldNames.contains(propertyName);
    }

    public boolean isTypeCodeField(String propertyName) {
        return typeCodeFieldNames.contains(propertyName);
    }

    public boolean isDeferredLoadField(Class<?> type) {
        return type.isAssignableFrom(Money.class) || ITypeCode.class.isAssignableFrom(type);
    }

    protected void initOptimizations() {
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (isDeferredLoadField(propertyDescriptor.getPropertyType())) {
                deferredLoadFieldNames.add(propertyDescriptor.getName());
            }
            if (propertyDescriptor.getPropertyType().isAssignableFrom(Money.class)) {
                moneyFieldNames.add(propertyDescriptor.getName());
            }
            if (ITypeCode.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                typeCodeFieldNames.add(propertyDescriptor.getName());
            }

            mapPropertyToColumnName(propertyDescriptor);
        }
        mapAliasPropertyNames();
    }

    protected Map<String, Column> mapFieldsToColumns(Class<?> resultClass) {
        fieldsToColumns = new LinkedHashMap<>();
        buildFieldColumnMap(fieldsToColumns, resultClass);
        fieldsToColumns = orderColumns(fieldsToColumns);
        return fieldsToColumns;
    }

    protected Map<String, Column> orderColumns(Map<String, Column> argFieldsToColumns) {
        Map<String, Column> orderedFieldsToColumns = new LinkedHashMap<>();

        // Primary keys first, then regular fields, maintenance fields last.
        argFieldsToColumns.entrySet().stream()
                .sorted( (o1, o2) -> o1.getValue().isPrimaryKey() ? 1 :
                                     !isRowMaintenanceColumn(o1.getKey()) ? 1 : 0)
                .forEach(entry -> orderedFieldsToColumns.put(entry.getKey(), entry.getValue()));
        return orderedFieldsToColumns;
    }

    protected void buildFieldColumnMap(Map<String, Column> fieldColumnMap, Class<?> clazz) {
        for (ModelClassMetaData classMetaData : getModelClassMetaData()) {
            Map<String, FieldMetaData> fieldMetaDatas = classMetaData.getEntityFieldMetaDatas();
            fieldMetaDatas.forEach((k,v)->fieldColumnMap.put(v.getField().getName(),v.getColumn()));
            if (ITaggedModel.class.isAssignableFrom(clazz)) {
                Column[] columns = classMetaData.getTable().getColumns();
                for (Column column : columns) {
                    if (column.getName().toUpperCase().startsWith(TagModel.TAG_PREFIX)) {
                        fieldColumnMap.put(column.getName(), column);
                    }
                }
            }
            if (IAugmentedModel.class.isAssignableFrom(clazz) && clazz.getAnnotation(Augmented.class) != null) {
                List<AugmenterConfig> configs = augmenterHelper.getAugmenterConfigs(clazz);
                if (CollectionUtils.isNotEmpty(configs)) {
                    for (AugmenterConfig config : configs) {
                        if (config != null && config.getPrefix() != null) {
                            Column[] columns = classMetaData.getTable().getColumns();
                            for (Column column : columns) {
                                if (column.getName().toUpperCase().startsWith(config.getPrefix())) {
                                    fieldColumnMap.put(column.getName(), column);
                                }
                            }
                        }
                        else {
                            log.debug("Missing augmenterConfig for class named " + clazz.getSimpleName());
                        }
                    }
                }
            }
        }
    }

    protected void mapAliasPropertyNames() {
        if (this.modelClassMetaData != null) {
            for (ModelClassMetaData m : this.modelClassMetaData) {
                for (Map.Entry<String, FieldMetaData> fieldMeta : m.getEntityFieldMetaDatas().entrySet()) {
                    if (!propertyToColumnNames.containsKey(fieldMeta.getKey())) {
                        propertyToColumnNames.put(fieldMeta.getKey(), fieldMeta.getValue().getColumn().getName());
                    }
                }
            }
        }
    }

    protected void mapPropertyToColumnName(PropertyDescriptor propertyDescriptor) {
        if (this.modelClassMetaData != null) {
            for (ModelClassMetaData m : this.modelClassMetaData) {
                Map<String, FieldMetaData> entityFieldMetaDatas = m.getEntityFieldMetaDatas();
                FieldMetaData d = entityFieldMetaDatas.get(propertyDescriptor.getName());
                if (d != null) {
                    propertyToColumnNames.put(propertyDescriptor.getName(), d.getColumn().getName());
                    break;
                }
            }
        }
    }

    protected boolean isRowMaintenanceColumn(String fieldName) {
        return fieldName.equals("createBy")
                || fieldName.equals("createTime")
                || fieldName.equals("updateBy")
                || fieldName.equals("updateTime");
    }
}
