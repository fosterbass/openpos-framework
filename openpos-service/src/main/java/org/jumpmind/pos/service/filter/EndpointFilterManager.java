package org.jumpmind.pos.service.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.service.EndpointInvocationContext;
import org.jumpmind.pos.service.PosServerException;
import org.jumpmind.pos.util.ObjectFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Version;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EndpointFilterManager {

    public static final Version MAX_VERSION = new Version(99999, 99999, 99999);
    public static final Version MIN_VERSION = new Version(0, 0, 0);
    public static final Version VERSION_ZERO = new Version(0, 0, 0);

    @Autowired
    private ApplicationContext applicationContext;

    private List<EndpointFilterTemplate> requestFilterTemplates = new ArrayList<>();
    private List<EndpointFilterTemplate> responseFilterTemplates = new ArrayList<>();

    @PostConstruct
    public void init() {
        Map<String, Object> filterBeans = applicationContext.getBeansWithAnnotation(EndpointFilter.class);

        if (!MapUtils.isEmpty(filterBeans)) {
            loadFilters(filterBeans);
        }
    }

    protected void loadFilters(Map<String, Object> requestFilterBeans) {
        for (Map.Entry<String, Object> beanEntry : requestFilterBeans.entrySet()) {
            loadFilter(beanEntry);
        }

        log.info("Found " + requestFilterTemplates.size() + " endpoint request filter(s) and " +
                responseFilterTemplates.size() + " response filter(s).");
        log.debug("requestFilterTemplates={}" + requestFilterTemplates);
        log.debug("responseFilterTemplates={}" + responseFilterTemplates);
    }

    protected void loadFilter(Map.Entry<String, Object> beanEntry) {
        for (Method method : ReflectionUtils.getUniqueDeclaredMethods(beanEntry.getValue().getClass())) {
            EndpointRequestFilter requestAnnotation = method.getAnnotation(EndpointRequestFilter.class);
            if (requestAnnotation != null) {
                requestFilterTemplates.add(buildFilterTemplate(beanEntry, method));
            }
            EndpointResponseFilter responseAnnotation = method.getAnnotation(EndpointResponseFilter.class);
            if (responseAnnotation != null) {
                responseFilterTemplates.add(buildFilterTemplate(beanEntry, method));
            }
        }
    }

    protected EndpointFilterTemplate buildFilterTemplate(Map.Entry<String, Object> beanEntry, Method method) {
        EndpointFilterTemplate filterTemplate = new EndpointFilterTemplate();
        filterTemplate.setFilterMethod(method);
        filterTemplate.setFilterInstance(beanEntry.getValue());

        if (method.getParameterTypes().length == 0) {
            throw new PosServerException("Invalid endpoint filter method, must declare at least 1 argument. " + method);
        }

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> clazz = method.getParameterTypes()[i];

            filterTemplate.setOutputType(method.getReturnType());

            if (!clazz.isAssignableFrom(RequestContext.class)
                    && !clazz.isAssignableFrom(EndpointInvocationContext.class)) {
                filterTemplate.setInputType(clazz);
            }
            switch (i) {
                case 0:
                    filterTemplate.setArg1Type(clazz);
                    break;
                case 1:
                    filterTemplate.setArg2Type(clazz);
                    break;
                default:
                    throw new PosServerException("Too many arguments on filter method. We support max 2 arguments, " +
                            "the EndpointInvocationContext and the target object to filter.");
            }
        }

        String minVersionString = null;
        String maxVersionString = null;

        EndpointRequestFilter requestAnnotation = method.getAnnotation(EndpointRequestFilter.class);
        if (requestAnnotation != null) {
            minVersionString = requestAnnotation.versionGreaterThan();
            maxVersionString = requestAnnotation.versionLessThan();
            filterTemplate.setPath(requestAnnotation.path());
        }
        EndpointResponseFilter responseAnnotation = method.getAnnotation(EndpointResponseFilter.class);
        if (responseAnnotation != null) {
            minVersionString = responseAnnotation.versionGreaterThan();
            maxVersionString = responseAnnotation.versionLessThan();
            filterTemplate.setPath(responseAnnotation.path());
        }

        Version minVersion = StringUtils.isNotEmpty(minVersionString) ?
                Version.parse(minVersionString) : MIN_VERSION;
        Version maxVersion = StringUtils.isNotEmpty(maxVersionString) ?
                Version.parse(maxVersionString) : MAX_VERSION;

        filterTemplate.setMinimumVersion(minVersion);
        filterTemplate.setMaximumVersion(maxVersion);

        return filterTemplate;
    }

    public Object filterRequest(RequestContext context) {
        if (CollectionUtils.isEmpty(requestFilterTemplates)) {
            return null;
        }

        Version version = StringUtils.isNotEmpty(context.getVersion()) ?
                Version.parse(context.getVersion()) :
                VERSION_ZERO;

        for (EndpointFilterTemplate filterTemplate : requestFilterTemplates) {

            if (StringUtils.isNotEmpty(filterTemplate.getPath())
                    && !filterTemplate.getPath().equals(context.getPath())) {
                continue;
            }

            // for a incoming request, match up the filter output to the target input.
            if (version.isGreaterThan(filterTemplate.getMinimumVersion())
                    && version.isLessThan(filterTemplate.getMaximumVersion())
                    && filterTemplate.getOutputType().isAssignableFrom(context.getInputType())) {

                // parse json to the filter's input  type.
                try {
                    Object argument = context.getObjectMapper().readValue(context.getJson(), filterTemplate.getInputType());
                    return invokeFilter(filterTemplate, context, argument);
                } catch (Exception ex) {
                    throw new PosServerException("Failed to prase json to " + filterTemplate.getInputType(), ex);
                }
            }
        }

        return null;
    }

    public void filterResponse(EndpointInvocationContext context) {
        if (CollectionUtils.isEmpty(responseFilterTemplates) || context.getResult() == null) {
            return;
        }

        Object newResult = context.getResult();

        for (EndpointFilterTemplate filterTemplate : responseFilterTemplates) {
            Object tempResult = filterResponse(context, filterTemplate, newResult);
            if (tempResult != null) {
                newResult = tempResult;
                context.setResult(newResult);
            }
        }
    }

    protected Object filterResponse(EndpointInvocationContext context, EndpointFilterTemplate filterTemplate, Object argument) {
        if (!versionApplies(context.getClientVersion(), filterTemplate) ||
                (StringUtils.isNotEmpty(filterTemplate.getPath()) && !filterTemplate.getPath().equals(context.getEndpointPath()))) {
            return null;
        }
        if (filterTemplate.getOutputType().isAssignableFrom(argument.getClass())) {
            return invokeFilter(filterTemplate, context, argument);
        } else {
            doRecursiveSearch(context, filterTemplate, argument);
        }
        return null;
    }

    private void doRecursiveSearch(EndpointInvocationContext context, EndpointFilterTemplate filterTemplate, Object argument) {
        ObjectFinder<?> finder = new ObjectFinder<>(filterTemplate.getOutputType());
        finder.searchRecursive(argument, (parentObject, targetObject, field) -> {
            if (parentObject == argument) {
                Object result = invokeFilter(filterTemplate, context, targetObject);
                if (result != null && result != targetObject) {
                    try {
                        field.setAccessible(true);
                        field.set(parentObject, result);
                    } catch (Exception ex) {
                        throw new PosServerException("Failed to replace nested object. Filter method " +
                                filterTemplate.getFilterMethod() + " attempted to replace " + targetObject + " with " + result, ex);
                    }
                }
            }
        });
    }

    protected boolean versionApplies(Version version, EndpointFilterTemplate filterTemplate) {
        if (version == null || version.equals(VERSION_ZERO)) {
            return false;
        } else {
            return version.isGreaterThan(filterTemplate.getMinimumVersion())
                    && version.isLessThan(filterTemplate.getMaximumVersion());
        }
    }

    protected Object invokeFilter(EndpointFilterTemplate filterTemplate, Object context, Object argument) {
        if (log.isDebugEnabled()) {
            log.debug("Invoking endpoint filter:" + filterTemplate + " context=" + context + " argument=" + argument);
        } else {
            log.info("Invoking endpoint filter: " + filterTemplate.getFilterMethod() + " argument " + argument);
        }

        Object[] args = null;
        if (filterTemplate.getArg1Type().isAssignableFrom(context.getClass())) {
            args = new Object[]{context, argument};
        } else {
            args = new Object[]{argument};
        }

        try {
            return filterTemplate.getFilterMethod().invoke(filterTemplate.getFilterInstance(), args);
        } catch (Exception ex) {
            throw new PosServerException("Failed to invoke filter method " + filterTemplate.getFilterMethod(), ex);
        }
    }
}