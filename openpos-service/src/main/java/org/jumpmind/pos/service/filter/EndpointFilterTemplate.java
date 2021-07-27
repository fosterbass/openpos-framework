package org.jumpmind.pos.service.filter;

import lombok.Data;
import org.springframework.data.util.Version;

import java.lang.reflect.Method;

@Data
public class EndpointFilterTemplate {

    private Class<?> inputType;
    private Class<?> outputType;
    private Method filterMethod;
    private Version minimumVersion;
    private Version maximumVersion;
    private String path;

    private Class<?> arg1Type;
    private Class<?> arg2Type;

    private Object filterInstance;

}
