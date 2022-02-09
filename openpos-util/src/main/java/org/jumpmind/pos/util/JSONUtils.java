package org.jumpmind.pos.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
@Slf4j
public class JSONUtils {

    public Map<String, String> toMap(String json) {
        try {
            return getObjectMapper().readValue(json, Map.class);
        } catch (Exception ex) {
            log.warn("Could not deserialize JSON string into a Map<String, String>: " + json, ex);
            return new HashMap<>();
        }
    }

    public <T> T toObject(String json, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(json, clazz);
        } catch (Exception ex) {
            log.warn(String.format("Could not deserialize JSON string:[%s] into an instance of %s", json, clazz.getName()), ex);
            return null;
        }
    }

    public String toJson(Object o) {
        return toJson(o, false);
    }

    public String toJson(Object o, boolean prettyPrint) {

        ObjectWriter writer = prettyPrint ? getObjectMapper().writerWithDefaultPrettyPrinter() : getObjectMapper().writer();

        if (o == null) {
            return "null";
        }
        try {
            return writer.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            log.warn("Could not serialize object: " + o, ex);
            return "";
        }
    }

    public ObjectMapper getObjectMapper() {
        return DefaultObjectMapper.defaultObjectMapper();
    }

}
