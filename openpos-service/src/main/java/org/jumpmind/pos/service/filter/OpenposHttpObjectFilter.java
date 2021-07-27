package org.jumpmind.pos.service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jumpmind.pos.service.PosServerException;
import org.jumpmind.pos.util.ClassUtils;
import org.jumpmind.pos.util.DefaultObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class OpenposHttpObjectFilter implements HandlerMethodArgumentResolver {

    private ObjectMapper objectMapper = DefaultObjectMapper.defaultObjectMapper();

    @Autowired
    EndpointFilterManager endpointFilterManager;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return !ClassUtils.isSimpleType(methodParameter.getParameterType());
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
        Class<?> outputType = methodParameter.getMethod().getReturnType();
        String json = IOUtils.toString(request.getInputStream(), "UTF-8");

        RequestContext context = RequestContext.builder()
                .objectMapper(objectMapper)
                .request(request)
                .targetMethod(methodParameter.getMethod())
                .path(path)
                .version(version)
                .inputType(inputType)
                .outputType(outputType)
                .json(json).build();

        try {
            Object result = endpointFilterManager.filterRequest(context);
            if (result == null) {
                result = objectMapper.readValue(json, inputType);
            }
            return result;
        } catch (Exception ex) {
            String msg = "Failed to deserialize json to type: " + inputType + " json: " + json;
            log.warn(msg, ex);
            throw new PosServerException(msg, ex);
        }
    }
}
