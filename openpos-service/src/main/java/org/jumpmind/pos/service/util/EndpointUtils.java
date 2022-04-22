package org.jumpmind.pos.service.util;

import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;

import static java.util.Arrays.stream;

/**
 * A set of utility methods facilitating the invocation of service endpoints.
 *
 * @author Jason Weiss
 */
@NoArgsConstructor(access = PRIVATE)
public class EndpointUtils {
    // TODO Document

    public static String getPathToEndpoint(Object service, Method serviceMethod) {
        final StringBuilder path = new StringBuilder();

        RequestMapping clazzMapping = getRequestMapping(service, serviceMethod);
        if ((clazzMapping != null) && (clazzMapping.path().length > 0)) {
            path.append(clazzMapping.path()[0]);
        }

        RequestMapping methodMapping = getMergedAnnotation(serviceMethod, RequestMapping.class);
        if ((methodMapping != null) && (methodMapping.path().length > 0)) {
            path.append(methodMapping.path()[0]);
        }
        return path.toString();
    }

    public static RestController getRestController(Object service, Method serviceMethod) {
        return getAnnotation(service, serviceMethod, RestController.class);
    }

    private static <A extends Annotation> A findAnnotation(Class<?>[] classes, Class<A> annotation) {
        return stream(classes).map(clz -> getMergedAnnotation(clz, annotation)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static <A extends Annotation> A getAnnotation(Object object, Method method, Class<A> annotation) {
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

    private static RequestMapping getRequestMapping(Object service, Method serviceMethod) {
        return getAnnotation(service, serviceMethod, RequestMapping.class);
    }
}
