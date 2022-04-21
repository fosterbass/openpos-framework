package org.jumpmind.pos.service;

import org.jumpmind.pos.service.filter.EndpointFilterManager;
import org.jumpmind.pos.service.strategy.IInvocationStrategy;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Version;

import java.lang.reflect.Method;
import java.util.List;

import static org.jumpmind.pos.service.ServiceConfig.LOCAL_PROFILE;

import static lombok.AccessLevel.NONE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * A model configuring a service invocation.
 */
@Builder
@Data
@Slf4j
public class EndpointInvocationContext {
    private static final EndpointInvocationContext empty = EndpointInvocationContext.builder().build();

    private final ServiceSpecificConfig config;
    private final Object proxy;
    private final Method method;
    private final String endpointImplementation;
    private final String clientVersionString;
    private final Version clientVersion;
    private final String endpointPath;
    private final Object endpoint;

    @Setter(NONE)
    private List<String> profileIds;

    private IInvocationStrategy strategy;
    private Object[] arguments;
    private Object result;

    /**
     * @return an immutable singleton representing an uninitialized service invocation context
     */
    public static EndpointInvocationContext empty() {
        return empty;
    }

    /**
     * Creates a new context representing the merging of this context with another one.  In all cases where the latter context identifies a
     * non-trivial value for a property, that value will be represented in the returned context; any value assigned to the same property in this
     * context will be discarded.
     * <p>
     * Neither this context nor {@code preferredContext} will be modified by this operation.
     *
     * @param preferredContext the invocation context whose properties will take precedence in the merged context
     * @return a new invocation context containing the union of all properties from this context and {@code preferredContext}, with all collisions
     * resolved in favor of {@code preferredContext}
     */
    public EndpointInvocationContext preferring(EndpointInvocationContext preferredContext) {
        return builder()
                .arguments(ArrayUtils.isNotEmpty(preferredContext.getArguments()) ? preferredContext.getArguments() : arguments)
                .clientVersion(firstNonNull(preferredContext.getClientVersion(), clientVersion))
                .clientVersionString(firstNonNull(preferredContext.getClientVersionString(), clientVersionString))
                .config(firstNonNull(preferredContext.getConfig(), config))
                .endpoint(firstNonNull(preferredContext.getEndpoint(), endpoint))
                .endpointImplementation(firstNonNull(preferredContext.getEndpointImplementation(), endpointImplementation))
                .endpointPath(defaultIfBlank(preferredContext.getEndpointPath(), endpointPath))
                .method(firstNonNull(preferredContext.getMethod(), method))
                .profileIds(hasOverridableProfiles() ? preferredContext.getProfileIds() : profileIds)
                .proxy(firstNonNull(preferredContext.getProxy(), proxy))
                .strategy(firstNonNull(preferredContext.getStrategy(), strategy))
                .build();
    }

    /**
     * Updates this context with the specified service method arguments.
     *
     * @param arguments the arguments to pass to the service method
     * @return this context, modified
     */
    public EndpointInvocationContext withArguments(Object... arguments) {
        this.arguments = arguments;
        return this;
    }

    /**
     * Assigns a result to this service invocation context.
     *
     * @param result the result of a service invocation performed within this context
     * @return this context, modified
     */
    public EndpointInvocationContext withResult(Object result) {
        this.result = result;
        return this;
    }

    /**
     * Updates this context with the specified service endpoint invocation strategy.
     *
     * @param strategy the strategy governing the service endpoint's invocation
     * @return this context, modified
     */
    public EndpointInvocationContext withStrategy(IInvocationStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    /**
     * Reports, for the sake of determining whether a preferred context's values should override this one's, if this context has no assigned profiles,
     * either explicitly or effectively.  This method will return {@code true} if any of the following are true:
     * <ul>
     * <li>this context's profile ID set is {@code null} or empty</li>
     * <li>this context contains only a single profile ID of {@link ServiceConfig#LOCAL_PROFILE}</li>
     * </ul>
     * <p>
     * The background here is that a number of services are configured to target a non-existent "local" profile.  I'm assuming this is being done to
     * override default behavior which targets remote endpoints, but profiles are currently not even referenced outside the context of a remote
     * invocation strategy, so this configuration would appear to be unnecessary/redundant.  To this point it's also been harmless.  Now, however,
     * we're trying to decide whether a profile dictated by a client's preferred context should override this one, and we need to answer in the
     * affirmative if the latter has this inert "local" profile.
     *
     * @return {@code true} if this context has no substantive invocation profiles assigned to it
     */
    private boolean hasOverridableProfiles() {
        return isEmpty(profileIds) || ((profileIds.size() == 1) && LOCAL_PROFILE.equalsIgnoreCase(profileIds.get(0)));
    }

    /**
     * A builder for an {@link EndpointInvocationContext} (overriding/extending the Lombok-generated one).
     */
    public static class EndpointInvocationContextBuilder {
        /**
         * @param clientVersionString a string describing the client's API version
         * @return this builder
         */
        public EndpointInvocationContextBuilder clientVersionString(String clientVersionString) {
            this.clientVersionString = clientVersionString;

            if (StringUtils.isNotEmpty(clientVersionString)) {
                clientVersion = Version.parse(clientVersionString);

                if (clientVersion.equals(EndpointFilterManager.VERSION_ZERO)) {
                    log.debug("Unparsable client version {}", clientVersionString);
                    clientVersion = null;
                }
            }
            return this;
        }
    }
}
