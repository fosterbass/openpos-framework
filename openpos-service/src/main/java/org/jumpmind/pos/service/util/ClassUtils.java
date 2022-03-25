package org.jumpmind.pos.service.util;

import lombok.NoArgsConstructor;
import org.jumpmind.pos.persist.model.ScriptVersionModel;
import org.jumpmind.pos.service.instrumentation.ServiceSampleModel;
import org.jumpmind.pos.service.model.ModuleModel;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ClassUtils {
    public static List<Class<?>> getClassesForPackageAndAnnotation(String packageName, Class<? extends Annotation> annotation) {
        List<Class<?>> classes = Arrays.asList(ModuleModel.class, ServiceSampleModel.class, ScriptVersionModel.class);

        return org.jumpmind.pos.util.ClassUtils.getClassesForPackageAndAnnotation(packageName, annotation, classes, null);
    }
}
