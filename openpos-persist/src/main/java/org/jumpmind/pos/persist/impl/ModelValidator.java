package org.jumpmind.pos.persist.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.jumpmind.pos.persist.ColumnDef;
import org.jumpmind.pos.persist.CompositeDef;
import org.jumpmind.pos.persist.PersistException;
import org.jumpmind.pos.persist.model.AugmenterConfig;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ModelValidator {

    public static void validate(ModelMetaData modelMeta, ModelClassMetaData classMeta) {
        checkOrphanedFields(modelMeta, classMeta);
        checkCrossRefFields(modelMeta, classMeta);
        checkPrimaryKeyFields(modelMeta, classMeta);
        checkAugmentedFields(modelMeta, classMeta);
    }

    protected static void checkOrphanedFields(ModelMetaData modelMeta, ModelClassMetaData classMeta) {

        List<Class<?>> compositeDefClasses;
        Class<?> modelClass = classMeta.getModelClass();

        for (Field field : modelClass.getDeclaredFields()) {
            ColumnDef columnAnnotation = field.getAnnotation(ColumnDef.class);
            if (columnAnnotation != null) {
                // an annotated column MUST have a getter/setter pair to be handled properly 
                // by the persistence layer.

                String fieldNameCapatalized = StringUtils.capitalize(field.getName());
                try {                    
                    Method setter = modelClass.getDeclaredMethod("set"+fieldNameCapatalized, field.getType());
                    if (setter == null) {
                        throw new PersistException("Failed to locate setter set"+fieldNameCapatalized);
                    }
                    String prefix = field.getType().isAssignableFrom(boolean.class) ? "is" : "get";
                    Method getter = modelClass.getDeclaredMethod(prefix+fieldNameCapatalized);
                    if (!getter.getReturnType().isAssignableFrom(field.getType())) {
                        throw new PersistException("getter has wrong return type. " + getter);
                    }
                } catch (Exception ex) {
                    throw new PersistException("Failed to locate required getter/setter pair for " + 
                            field.getName() + " on model " + modelClass + ". Make sure your model class as the proper getter/setter for @ColumnDef field " + field.getName() + " (" + ex.toString() + ")");
                }
            }
        }
    }

    protected static void checkCrossRefFields(ModelMetaData modelMeta, ModelClassMetaData classMeta) {
        Class<?> modelClass = classMeta.getModelClass();
        List<Class<?>> compositeDefClasses = getCompositeDefClasses(modelClass);
        for (Field field : modelClass.getDeclaredFields()) {
            ColumnDef columnAnnotation = field.getAnnotation(ColumnDef.class);
            if (columnAnnotation != null) {
                if (field.getType().isAssignableFrom(Money.class)) {
                    if (StringUtils.isEmpty(columnAnnotation.crossReference()) 
                            && columnAnnotation.crossReferences().length == 0) {
                        throw new PersistException("columns of Money type require a ColumnDef with crossReference, "
                                + "such as @ColumnDef(crossReference=\"isoCurrencyCode\"). see " + field.getName() + " on model " + modelClass  );
                    }
                }
                if (!StringUtils.isEmpty(columnAnnotation.crossReference())) {

                    Optional<PropertyDescriptor> property =
                            Arrays.stream(modelMeta.getPropertyDescriptors()).filter(p -> columnAnnotation.crossReference().equals(p.getName())).findFirst();

                    if (!property.isPresent()) {
                        throw new PersistException("No matching field found for ColumnDef crossReference=\"" + columnAnnotation.crossReference() +
                                "\" see the \"" + field.getName() + "\" field on model " + modelClass);
                    }
                }
            }
        }    
    }

    protected static void checkAugmentedFields(ModelMetaData metaData, ModelClassMetaData meta) {
        if (CollectionUtils.size(meta.getAugmenterConfigs()) > 1) {
            Map<String, Integer> augmenterNameCounts = new HashMap<>();
            for (AugmenterConfig config : meta.getAugmenterConfigs()) {
                for (String name : config.getAugmenterNames()) {
                    Integer count = augmenterNameCounts.get(name);
                    if (count == null) {
                        count = 1;
                    }
                    else {
                        count++;
                    }
                    augmenterNameCounts.put(name, count);
                }
            }
            for (Map.Entry<String, Integer> entry : augmenterNameCounts.entrySet()) {
                if (entry.getValue() > 1) {
                    throw new PersistException("Duplicate augmenter name " + entry.getKey() + " found on model " + meta.getModelClass());
                }
            }
        }
    }

    private static void checkPrimaryKeyFields(ModelMetaData metaData, ModelClassMetaData meta) {
        Set<String> pkFieldNames = meta.getPrimaryKeyFieldNames();

        for (String pkFieldName : pkFieldNames) {
            FieldMetaData fieldMetaData = meta.getFieldMetaData(pkFieldName);
            if (fieldMetaData == null) {
                throw new PersistException("Model class " + meta.getModelClass().getSimpleName() +
                        " declares a primary key field called \"" + pkFieldName + "\" but does not define a field by that name.");
            }

        }
    }

    private static Field getCrossReferenceField(Class<?> modelClass, ColumnDef columnAnnotation) throws NoSuchFieldException{
            return modelClass.getDeclaredField(columnAnnotation.crossReference());
    }

    private static List<Class<?>> getCompositeDefClasses(Class<?> clazz) {
        ArrayList<Class<?>> compositeDefClasses = new ArrayList<Class<?>>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            CompositeDef compositeDefAnnotation = field.getAnnotation(CompositeDef.class);
            if (compositeDefAnnotation != null) {
                compositeDefClasses.add(field.getType());
                getCompositeDefClasses(field.getType());
            }
        }
        return compositeDefClasses;
    }
}
    
