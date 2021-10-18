package org.jumpmind.pos.util;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Slf4j
public final class RestApiSupport {
    public static final String REST_API_CONTEXT_PATH = "/rest";

    public static final String REST_API_TOKEN_HEADER_NAME = "X-API-Token";
    /**
     * @see org.jumpmind.pos.util.ApiTokenGenerator#generateApiToken()
     */
    public static final String REST_API_LOOPBACK_TOKEN = "LjVHdqj47ecbHVnkBB35";

    private static final UnaryOperator<String> REQUEST_MAPPING_TO_WILDCARD_URI_PATTERN = requestMapping -> requestMapping + "/*";

    public static String generateApiToken() {
        return new ApiTokenGenerator().generateApiToken();
    }

    public static Set<Class<?>> findRestApiControllers(final String packageName) {
        return findRestApiControllers(packageName, newRestApiScanner());
    }

    public static Set<Class<?>> findRestApiControllers(final String packageName, final ClassPathScanningCandidateComponentProvider scanner) {
        return scanner.findCandidateComponents(packageName).stream().
                map(BeanDefinition::getBeanClassName).
                map(RestApiSupport::classForName).
                filter(Objects::nonNull).
                collect(Collectors.toSet());
    }

    public static ClassPathScanningCandidateComponentProvider newRestApiScanner() {
        return newRestApiScanner(Collections.emptySet());
    }

    public static ClassPathScanningCandidateComponentProvider newRestApiScanner(final Set<String> ignorePackages) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            /*
             * stock Spring impl will refuse to yield interfaces or abstract
             * classes, but OpenPOS and JMC typically apply the @Api and
             * @RestController annotations to interfaces
             */
            @Override
            protected boolean isCandidateComponent(final AnnotatedBeanDefinition definition) {
                /*
                 * at this point, exclusion & inclusion filters have already
                 * been applied
                 */
                return super.isCandidateComponent(definition) || definition.getMetadata().isIndependent();
            }
        };

        /*
         * avoid loading any class from the POS wrapper package (some of these
         * classes load native shared libraries, which may fail)
         *
         * use the AspectJTypeFilter, specifically, for the exclusion filter
         * because it can disqualify candidates WITHOUT loading the class
         * (native shared libs are loaded from a <clinit>, so any filter that
         * must load the class first can't be used here)
         *
         * note that the "within(...)" construct is (errantly?) producing
         * "warning no match for this type name: within [Xlint:invalidAbsoluteTypeName]"
         * during build (however, the filter itself still works)
         */
        StringBuilder ignoreExpression = new StringBuilder();
        for (String ignorePackageName : ignorePackages) {
            if (ignoreExpression.length() > 0) ignoreExpression.append(" || ");
            ignoreExpression.append("within(").append(ignorePackageName).append("..*)");
        }
        /*
         * this filter will be consulted to disqualify candidates BEFORE any
         * inclusion filters
         */
        scanner.addExcludeFilter(new AspectJTypeFilter(ignoreExpression.toString(), Thread.currentThread().getContextClassLoader()));

        /*
         * we are interested in @Api- AND @RestController-annotated components,
         * specifically; when running tests, we don't want the scanner to pick
         * up test fixtures that are only @RestController-annotated
         */
        TypeFilter apiAnnotated = new AnnotationTypeFilter(Api.class);
        TypeFilter restControllerAnnotated = new AnnotationTypeFilter(RestController.class);
        TypeFilter apiAndRestControllerAnnotated = (mr, mrf) -> apiAnnotated.match(mr, mrf) && restControllerAnnotated.match(mr, mrf);
        scanner.addIncludeFilter(apiAndRestControllerAnnotated);

        return scanner;
    }

    /*
     * can't use ClassUtils#resolveAnnotation because it assumes it is being
     * passed an instance whereas here we only have the interface class
     */
    public static RequestMapping resolveRequestMapping(final Class<?> restApiClass) {
        return restApiClass.getAnnotation(RequestMapping.class);
    }

    public static String requestMappingToUrlPattern(final String requestMapping) {
        return requestMappingToUrlPattern(requestMapping, REQUEST_MAPPING_TO_WILDCARD_URI_PATTERN);
    }

    public static String requestMappingToUrlPattern(final String requestMapping, final UnaryOperator<String> converter) {
        return converter.apply(requestMapping);
    }

    public static String legacyContextPath(final String restApiRequestMapping) {
        if (!restApiRequestMapping.startsWith(REST_API_CONTEXT_PATH + "/")) return null;

        String legacyRequestMapping = restApiRequestMapping.substring(REST_API_CONTEXT_PATH.length());
        int contextSlash = legacyRequestMapping.indexOf('/', 1);
        return contextSlash == -1 ? legacyRequestMapping : legacyRequestMapping.substring(0, contextSlash);
    }

    public static boolean isLegacyClient(final HttpServletRequest request) {
        String jmcVersion = request.getHeader("ClientContext-version.nu-commerce");
        if (jmcVersion == null || jmcVersion.isEmpty()) {
            log.debug("request is missing ClientContext-version.nu-commerce header");
            return false;
        }
        String[] parts = jmcVersion.split("\\.");
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            if (major < 1) {
                return true;
            } else if (major == 1) {
                return minor < 12;
            } else {
                return false;
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
            log.warn("unexpected ClientContext-version.nu-commerce header format ({})", ex.toString());
            return false;
        }
    }

    private static Class<?> classForName(final String className) {
        try {
            return ClassUtils.loadClass(className);
        } catch (Exception ex) {
            log.warn("failed to load class {}: {}", className, ex.toString());
            return null;
        }
    }

    private RestApiSupport() {
        /* do not instantiate */
    }
}
