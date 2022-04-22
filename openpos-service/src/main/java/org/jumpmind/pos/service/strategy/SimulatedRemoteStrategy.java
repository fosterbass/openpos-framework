package org.jumpmind.pos.service.strategy;

import org.jumpmind.pos.service.EndpointInvocationContext;
import org.jumpmind.pos.util.DefaultObjectMapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.function.Failable;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedParameterizedType;
import java.util.List;

import static java.util.Arrays.stream;

@Component(SimulatedRemoteStrategy.SIMULATED_REMOTE_STRATEGY)
public class SimulatedRemoteStrategy extends LocalOnlyStrategy {

    static final String SIMULATED_REMOTE_STRATEGY = "SIMULATED_REMOTE";

    @Override
    public String getStrategyName() {
        return SIMULATED_REMOTE_STRATEGY;
    }

    @Override
    public Object invoke(EndpointInvocationContext endpointInvocationContext) throws Throwable {
        final ObjectMapper mapper = DefaultObjectMapper.build();
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        final Object[] arguments = (endpointInvocationContext.getArguments() == null)
                ? null
                : stream(endpointInvocationContext.getArguments())
                        .map(Failable.asFunction(arg -> mapper.readValue(mapper.writeValueAsString(arg), arg.getClass())))
                        .toArray();

        final Object retObj = super.invoke(endpointInvocationContext.withArguments(arguments));

        if (retObj instanceof List<?>) {
            final String className = ((AnnotatedParameterizedType) endpointInvocationContext.getMethod().getAnnotatedReturnType())
                    .getAnnotatedActualTypeArguments()[0]
                    .getType()
                    .getTypeName();

            return mapper.readValue(
                    mapper.writeValueAsString(retObj),
                    mapper.getTypeFactory().constructCollectionType(List.class, Class.forName(className)));
        }
        return (retObj != null)
                ? mapper.readValue(mapper.writeValueAsString(retObj), retObj.getClass())
                : null;
    }
}
