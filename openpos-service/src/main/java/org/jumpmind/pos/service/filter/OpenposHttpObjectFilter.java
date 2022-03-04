package org.jumpmind.pos.service.filter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.service.PosServerException;
import org.jumpmind.pos.util.ClassUtils;
import org.jumpmind.pos.util.DefaultObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;


@Slf4j
@Component
public class OpenposHttpObjectFilter implements HandlerMethodArgumentResolver {

    @Autowired
    EndpointFilterManager endpointFilterManager;
    private ObjectMapper objectMapper = DefaultObjectMapper.defaultObjectMapper();

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return !ClassUtils.isSimpleType(methodParameter.getParameterType())
                && methodParameter.getParameterAnnotation(RequestBody.class) != null;
    }

    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) throws Exception {

        HttpServletRequest request
                = (HttpServletRequest) nativeWebRequest.getNativeRequest();

        String path = request.getServletPath();
        String version = nativeWebRequest.getHeader("ClientContext-version.nu-commerce");
        Class<?> inputType = methodParameter.getParameterType();
        Method method = methodParameter.getMethod();
        Class<?> outputType = method != null ? method.getReturnType() : null;
        String json = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        JavaType javaType = getJavaType(methodParameter);
        RequestContext context = RequestContext.builder()
                .objectMapper(objectMapper)
                .request(request)
                .targetMethod(method)
                .path(path)
                .version(version)
                .inputType(inputType)
                .outputType(outputType)
                .json(json)
                .javaType(javaType)
                .methodParameter(methodParameter)
                .build();

        try {
            Object result = endpointFilterManager.filterRequest(context);
            if (result == null) {
                if (StringUtils.isEmpty(json)) {
                    result = null;
                } else {
                    result = objectMapper.readValue(json, context.getJavaType());
                }
            }
            return result;
        } catch (Exception ex) {
            String msg = "Failed to deserialize json to type: " + inputType + " json: " + json;
            log.warn(msg, ex);
            throw new PosServerException(msg, ex);
        }
    }

    private JavaType getJavaType(MethodParameter param) {
            param = param.nestedIfOptional();

            Type genericParameterType = param.getNestedGenericParameterType();
            Class<?> contextClass = param.getContainingClass();
            Type type = GenericTypeResolver.resolveType(genericParameterType, contextClass);
            return this.objectMapper.getTypeFactory().constructType(type);

    }
}