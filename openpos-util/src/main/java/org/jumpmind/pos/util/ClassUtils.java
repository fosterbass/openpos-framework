package org.jumpmind.pos.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.money.Money;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@Slf4j
public class ClassUtils {
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String className) {
        if (className == null) {
            throw new ReflectionException("className cannot be null.");
        }
        try {
            return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (Exception ex) {
            throw new ReflectionException("Failed to load class named \"" + className + "\"", ex);
        }
    }

    public static <T> T instantiate(String className) {
        Class<T> clazz = loadClass(className);
        if (clazz == null) {
            throw new ReflectionException("No class found for className:\"" + className + "\"");
        }
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new ReflectionException("Failed to instansitate class named \"" + className + "\"", ex);
        }
    }

    /**
     * This method first attempts to check the given targetObject's class for an
     * annotation of the given type.  If that fails, then it uses a Spring AOP
     * Utility to attempt to locate the annotation.  This is useful for CGLIB
     * proxies who don't actually have the annotation of the proxied bean
     * on them, and therefore the actual class being proxied needs to be checked
     * for the annotation.
     *
     * @param annotationClass The annotation type to search for.
     * @param targetObj The object whose class should be searched for the given
     * annotation type.
     * @return Will return null if the annotation could not found. Otherwise,
     * if the annotation exists on the class of the given targetObj, it will be
     * returned.
     */
    public static <A extends Annotation> A resolveAnnotation(Class<A> annotationClass, Object targetObj) {
        A annotation = targetObj.getClass().getAnnotation(annotationClass);
        if (annotation == null) {
            Class<?> targetClass = AopUtils.getTargetClass(targetObj);
            annotation = targetClass.getAnnotation(annotationClass);
        }

        return annotation;
    }

    /**
     * Retrieves all of the classes at or below the given package which have the
     * given annotation.
     *
     * @param packageName The root package to begin searching
     * @param annotation The annotation to search for.
     * @return A list of Class objects.
     */
    public static List<Class<?>> getClassesForPackageAndAnnotation(String packageName, Class<? extends Annotation> annotation) {
        return getClassesForPackageAndAnnotation(packageName, annotation, null, null);
    }

    /**
     * Retrieves all of the classes at or below the given package which implement the given interface.
     *
     * @param packageName The root package to begin searching
     * @param matchingType The annotation to search for.
     * @return A list of Class objects.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Class<T>> getClassesForPackageAndType(String packageName, Class<T> matchingType) {
        return getClassesForPackageAndAnnotation(packageName, null, null, matchingType).stream()
                .map(clazz -> (Class<T>) clazz)
                .collect(toList());
    }

    /**
     * Retrieves all of the classes at or below the given package which are in the list of class names provided and have
     * the given annotation
     *
     * @param packageName The root package to begin searching
     * @param classNames The list of names to include
     * @param annotation The annotation to search for
     * @return all classes at or below {@code packageName}
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Class<T>> getClassForPackageAndClassNamesAndAnnotation(String packageName, List<String> classNames, Class<? extends Annotation> annotation) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        if (annotation != null) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
        }

        return scanner.findCandidateComponents(packageName).stream()
                .filter(beanDef -> classNames.contains(beanDef.getBeanClassName()))
                .map(beanDef -> {
                    try {
                        return (Class<T>) Class.forName(beanDef.getBeanClassName());
                    } catch (ClassNotFoundException | NoClassDefFoundError ex) {
                        log.error("Could not load class " + beanDef.getBeanClassName(), ex);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    /**
     * Retrieves all of the classes at or below the given package which have the given annotation OR implement the matchingInterface.
     *
     * @param packageName The root package to begin searching
     * @param annotation The annotation to search for (optional)
     * @param alwaysIncludeClasses An optional list of classes to always return in the list of returned classes.
     * @return A list of Class objects.
     */
    public static List<Class<?>> getClassesForPackageAndAnnotation(String packageName, Class<? extends Annotation> annotation, List<Class<?>> alwaysIncludeClasses, Class<?> matchingType) {
        List<Class<?>> classes = new ArrayList<>();
        if (alwaysIncludeClasses != null) {
            classes.addAll(alwaysIncludeClasses);
        }
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        if (annotation != null) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
        }
        if (matchingType != null) {
            scanner.addIncludeFilter(new AssignableTypeFilter(matchingType));
        }

        for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
            try {
                classes.add(Class.forName(bd.getBeanClassName()));
            } catch (ClassNotFoundException | NoClassDefFoundError ex) {
                log.error("Could not load class " + bd.getBeanClassName(), ex);
            }
        }
        return classes;
    }


    public static boolean isSimpleType(Class<?> clazz) {
        return (clazz.isPrimitive()
                || String.class == clazz
                || BigDecimal.class == clazz
                || Money.class == clazz
                || Date.class == clazz);
    }
}
