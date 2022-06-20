package org.jumpmind.pos.service;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.util.ClassUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.h2.value.CaseInsensitiveMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.jumpmind.pos.service.Endpoint.IMPLEMENTATION_DEFAULT;
import static org.jumpmind.pos.service.Endpoint.IMPLEMENTATION_TRAINING;
import static org.jumpmind.pos.service.util.EndpointUtils.getPathToEndpoint;

import static org.apache.commons.lang3.StringUtils.isBlank;

import static java.util.stream.Collectors.toList;

/**
 * A self-populating repository which maps service interface methods to the microservice endpoints implementing them.
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class EndpointManager {
    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^(?<service>[^_]+)(_(?<version>\\d(_\\d)*))?$");
    private static final String IMPLEMENTATION_CONFIG_PATH = "openpos.services.specificConfig.%s.implementation";

    private final ApplicationContext applicationContext;
    private final Environment env;

    private Map<String, Object> endPointsByPath;
    private Map<String, Object> trainingEndPointsByPath;
    private Map<String, String> serviceImplementationMap = new HashMap<>();

    public void buildEndpointMappingsForService(Object service) {
        buildEndpointMappingsForService(service, null);
    }

    /**
     * Derives and stores all path-to-endpoint mappings for the specified service.  A "service" in this context is any interface or class bearing a
     * {@link @RestController} annotation.
     *
     * @param service the service to map endpoints to.
     *
     * @param implementationOverride an optional service implementation to override the implementation specified in the configuration
     */
    public void buildEndpointMappingsForService(Object service, String implementationOverride) {
        final Collection<Object> endpointOverrides = applicationContext.getBeansWithAnnotation(EndpointOverride.class).values();
        final Collection<Object> endpointsObjects = applicationContext.getBeansWithAnnotation(Endpoints.class).values();
        Collection<Object> endpointObjects = applicationContext.getBeansWithAnnotation(Endpoint.class).values();

        /* If we have any endpoints that are an @EndpointOverride and extend an Endpoint with @Endpoint they were included
         * in the collection of endpoints and should therefore be filtered out. */
        endpointObjects = Stream.concat(endpointObjects.stream(), endpointsObjects.stream())
                .filter(e -> !endpointOverrides.contains(e))
                .collect(toList());

        for (Class<?> serviceInterface : service.getClass().getInterfaces()) {
            RestController controller = serviceInterface.getAnnotation(RestController.class);

            if (controller != null) {
                buildEndpointMappingsForController(service, serviceInterface, endpointOverrides, endpointObjects, controller, implementationOverride);
            }
        }
    }

    public Object getEndpointObject(String implementation, String path) {
        final Map<String, Object> endpointsByImplementation = (IMPLEMENTATION_TRAINING.equals(implementation))
                ? trainingEndPointsByPath
                : endPointsByPath;

        return endpointsByImplementation.get(path);
    }

    @EventListener
    void onApplicationEvent(ContextRefreshedEvent event) {
        if (endPointsByPath == null) {
            endPointsByPath = new HashMap<>();
            trainingEndPointsByPath = new HashMap<>();

            applicationContext.getBeansWithAnnotation(RestController.class).values().forEach(m -> buildEndpointMappingsForService(m, null));
        }
    }

    private void buildEndpointMappingsForController(
            Object service,
            Class<?> serviceInterface,
            Collection<Object> endpointOverrides,
            Collection<Object> endpointObjects,
            RestController controller,
            String implementationOverride) {

        final String serviceName = controller.value();
        final String serviceTypeName = serviceInterface.getSimpleName();
        final String implementation = StringUtils.isNotBlank(implementationOverride) ? implementationOverride : getServiceImplementation(serviceName);

        if ((implementation != null) && !implementation.equals(IMPLEMENTATION_DEFAULT)) {
            log.info("Loading endpoints for the '{}' implementation of {} ({})", implementation, serviceTypeName, serviceName);
        }
        else {
            log.debug("Loading endpoints for the '{}' implementation of {} ({})", implementation, serviceTypeName, serviceName);
        }


        serviceImplementationMap.put(serviceName, implementation);

        // For each endpoint, see if there is an override or special Training Mode version.
        // Build out lists for both regular operations and Training Mode.

        for (Method serviceMethod : serviceInterface.getMethods()) {
            buildEndpointMappingsForMethod(service, serviceMethod, serviceTypeName, endpointOverrides, endpointObjects, implementation);
        }
    }

    public String getCurrentServiceImplementation(Object service) {
        for (Class<?> serviceInterface : service.getClass().getInterfaces()) {
            RestController controller = serviceInterface.getAnnotation(RestController.class);

            if (controller != null) {
                return serviceImplementationMap.get(controller.value());
            }
        }
        return null;
    }

    private void buildEndpointMappingsForMethod(
            Object service,
            Method serviceMethod,
            String serviceTypeName,
            Collection<Object> endpointOverrides,
            Collection<Object> endpointObjects,
            String implementation) {

        String path = getPathToEndpoint(service, serviceMethod);

        //  See if there is an endpoint override bean for this service and path, both normal and Training Mode.

        Object regularEndpointOverrideBean = findBestEndpointOverrideMatch(path, implementation, endpointOverrides);
        Object trainingEndpointOverrideBean = findBestEndpointOverrideMatch(path, IMPLEMENTATION_TRAINING, endpointOverrides);

        //  Now see if there is a standard endpoint bean for this service and path. Again, both normal and
        //  Training Mode.

        Object regularEndpointBean = findMatch(path, endpointObjects, implementation);
        String regularEndpointImplementation = implementation;

        if (regularEndpointBean == null) {
            //  Nothing for the current implementation, so try the default.
            regularEndpointBean = findMatch(path, endpointObjects, IMPLEMENTATION_DEFAULT);
            regularEndpointImplementation = IMPLEMENTATION_DEFAULT;
        }

        if (regularEndpointBean == null) {
            log.warn("No endpoint match found for service {}, path '{}', implementation '{}'", serviceTypeName, path, implementation);
        }

        Object trainingEndpointBean = findMatch(path, endpointObjects, IMPLEMENTATION_TRAINING);
        if ((trainingEndpointBean != null) && (regularEndpointBean == null)) {
            log.warn("Endpoint match found for service {}, path '{}', implementation 'training', but not implementation '{}' or default",
                    serviceTypeName, path, implementation);
        }

        //  Given the endpoint beans we discovered above, decide which one will be
        //  used in both normal scenarios and Training Mode.

        if (trainingEndpointOverrideBean != null) {
            log.info("Training override endpoint bean for service {}, path {}, implementation {}", serviceTypeName, path, IMPLEMENTATION_TRAINING);
            trainingEndPointsByPath.put(path, trainingEndpointOverrideBean);

            if (regularEndpointOverrideBean != null) {
                log.info("Regular override endpoint bean for service {}, path {}, implementation {}", serviceTypeName, path, implementation);
                endPointsByPath.put(path, regularEndpointOverrideBean);
            }
        }
        else if (regularEndpointOverrideBean != null) {
            log.debug("Regular override endpoint bean for service {}, path {}, implementation {}", serviceTypeName, path, implementation);

            endPointsByPath.put(path, regularEndpointOverrideBean);
            trainingEndPointsByPath.put(path, regularEndpointOverrideBean);
        }
        else if (trainingEndpointBean != null) {
            log.info("Training endpoint bean for service {}, path {}, implementation {}", serviceTypeName, path, IMPLEMENTATION_TRAINING);
            trainingEndPointsByPath.put(path, trainingEndpointBean);

            if (regularEndpointBean != null) {
                log.info("Regular endpoint bean for service {}, path {}, implementation {}", serviceTypeName, path, regularEndpointImplementation);
                endPointsByPath.put(path, regularEndpointBean);
            }
        }
        else if (regularEndpointBean != null) {
            log.debug("Regular endpoint bean for service {}, path {}, implementation {}", serviceTypeName, path, regularEndpointImplementation);

            endPointsByPath.put(path, regularEndpointBean);
            trainingEndPointsByPath.put(path, regularEndpointBean);
        }
        else {
            log.warn(String.format(
                    "No endpoint defined for path '%s' in the '%s' service.  Please define a Spring-discoverable @Endpoint class, " +
                            "with a method annotated like @Endpoint(\"%s\")", path, serviceTypeName, path));
        }
    }

    private Object findBestEndpointOverrideMatch(String path, String implementation, Collection<Object> endpointOverrides) {
        Object bestMatch = null;

        final List<SimpleEntry<Object, EndpointOverride>> pathMatchedOverrides = endpointOverrides.stream()
                .map(o -> new SimpleEntry<>(o, ClassUtils.resolveAnnotation(EndpointOverride.class, o)))
                .filter(entry -> entry.getValue().path().equals(path))
                .collect(toList());

        if (!pathMatchedOverrides.isEmpty()) {
            final String overrideImplementation = pathMatchedOverrides.stream()
                    .map(entry -> entry.getValue().implementation())
                    .filter(impl -> impl.equals(implementation))
                    .findFirst()
                    .orElse(IMPLEMENTATION_DEFAULT);

            final List<SimpleEntry<Object, EndpointOverride>> implMatchedOverrides = pathMatchedOverrides.stream()
                    .filter(entry -> entry.getValue().implementation().equals(overrideImplementation))
                    .collect(toList());

            if (implMatchedOverrides.size() > 1) {
                throw new IllegalStateException(
                        String.format("Found %d EndpointOverrides having path '%s' and implementation '%s'. Expected only one.",
                                implMatchedOverrides.size(), path, implementation)
                );
            }
            else if (implMatchedOverrides.size() == 1) {
                log.info("Endpoint at path '{}' overridden with {}", path, implMatchedOverrides.get(0).getKey().getClass().getName());
                bestMatch = implMatchedOverrides.get(0).getKey();
            }
        }
        return bestMatch;
    }

    private Object findMatch(String path, Collection<Object> endpoints, String implementation) {
        for (Object endpointBean : endpoints) {
            final Map<String, Endpoint> allEndpoints = new CaseInsensitiveMap<>();

            Endpoint endpointAnnotation = ClassUtils.resolveAnnotation(Endpoint.class, endpointBean);
            Endpoints endpointsAnnotation = ClassUtils.resolveAnnotation(Endpoints.class, endpointBean);

            if (endpointAnnotation != null && endpointsAnnotation != null) {
                log.warn(
                        "Class `{}` has both the @Endpoint and @Endpoints annotation; a single annotation choice should be made but will resolve both",
                        endpointBean.getClass().getSimpleName());
            }

            if (endpointAnnotation != null) {
                final String key = endpointAnnotation.implementation() + ":" + endpointAnnotation.path();
                allEndpoints.put(key, endpointAnnotation);
            }

            if (endpointsAnnotation != null) {
                for (Endpoint epa : endpointsAnnotation.value()) {
                    final String key = epa.implementation() + ":" + epa.path();
                    allEndpoints.put(key, epa);
                }
            }

            final String expectedKey = implementation + ":" + path;

            if (allEndpoints.isEmpty()) {
                log.warn("No @Endpoint annotation found for endpoint class {}, path {}, implementation {}", endpointBean.getClass().getSimpleName(),
                        path, implementation);
            }
            else if (allEndpoints.containsKey(expectedKey)) {
                return endpointBean;
            }
        }
        return null;
    }

    private String getServiceImplementation(String serviceName) {
        String implementation = env.getProperty(String.format(IMPLEMENTATION_CONFIG_PATH, serviceName));

        if (isBlank(implementation)) {
            Matcher serviceNameMatcher = SERVICE_NAME_PATTERN.matcher(serviceName);

            if (serviceNameMatcher.matches()) {
                String versionLessServiceName = serviceNameMatcher.group("service");
                implementation = env.getProperty(String.format(IMPLEMENTATION_CONFIG_PATH, versionLessServiceName), IMPLEMENTATION_DEFAULT);
            }
        }
        return implementation;
    }
}
