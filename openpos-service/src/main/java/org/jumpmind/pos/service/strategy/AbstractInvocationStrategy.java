package org.jumpmind.pos.service.strategy;

import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public abstract class AbstractInvocationStrategy {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ApplicationContext applicationContext;

    public static String buildPath(Object object, Method method) {
        StringBuilder path = new StringBuilder();
        RequestMapping clazzMapping = getRequestMapping(object, method);

        if (clazzMapping != null) {
            path.append(clazzMapping.value()[0]);
        }

        RequestMapping methodMapping = getMergedAnnotation(method, RequestMapping.class);
        if ((methodMapping != null) && (methodMapping.path().length > 0)) {
            path.append(methodMapping.path()[0]);
        }
        return path.toString();
    }

    public static <A extends Annotation> A getAnnotation(Object object, Method method, Class<A> annotation) {
        Class<?> methodClazz = method.getDeclaringClass();
        A a = getMergedAnnotation(methodClazz, annotation);

        if (a == null) {
            Class<?>[] interfaces = methodClazz.getInterfaces();
            a = findAnnotation(interfaces, annotation);

            if (a == null) {
                interfaces = object.getClass().getInterfaces();
                a = findAnnotation(interfaces, annotation);
            }
        }
        return a;
    }

    static <A extends Annotation> A findAnnotation(Class<?>[] classes, Class<A> annotation) {
        A a = null;
        for (Class<?> clazz : classes) {
            a = getMergedAnnotation(clazz, annotation);
            if (a != null) {
                break;
            }
        }
        return a;
    }

    public static RequestMapping getRequestMapping(Object object, Method method) {
        return getAnnotation(object, method, RequestMapping.class);
    }

    public static RestController getRestController(Object object, Method method) {
        return getAnnotation(object, method, RestController.class);
    }

    public static String getServiceName(Object object, Method method) {
        RestController restController = getRestController(object, method);
        return (restController != null) ? restController.value() : "";
    }
}
