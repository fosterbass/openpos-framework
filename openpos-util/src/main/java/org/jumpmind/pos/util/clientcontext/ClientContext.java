package org.jumpmind.pos.util.clientcontext;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jumpmind.pos.util.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ClientContext {
    public static final String BUSINESS_UNIT_ID = "businessUnitId";
    public static final String TIMEZONE_OFFSET = "timezoneOffset";
    public static final String DEVICE_ID = "deviceId";
    private ThreadLocal<Map<String, String>> propertiesMap = new ThreadLocal<>();
    final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${openpos.installationId:'not set'}")
    String installationId;

    // Default businessUnitId if not set via personalization
    @Value("${openpos.businessunitId:'undefined'}")
    String businessUnitId;

    @Value("${openpos.deviceMode:'not set'}")
    String deviceMode;

    public void put(String name, String value) {
        if (propertiesMap.get() == null) {
            propertiesMap.set(new CaseInsensitiveMap<>());
        }

        if ("deviceMode".equalsIgnoreCase(name))  {
            value = ((value == null) || value.equals("'not set'") ? "default" : value);
        }

        propertiesMap.get().put(name, value);
    }

    public String get(String name) {
        Map<String, String> props = propertiesMap.get();

        if (props == null || !props.containsKey(name)) {
            if ("deviceId".equalsIgnoreCase(name)) {
                return installationId;
            } else if (BUSINESS_UNIT_ID.equalsIgnoreCase(name)) {
                return businessUnitId;
            } else if ("appId".equalsIgnoreCase(name)) {
                return "server";
            } else if ("deviceMode".equalsIgnoreCase(name)) {
                return deviceMode;
            } else if (TIMEZONE_OFFSET.equalsIgnoreCase(name)) {
                return AppUtils.getTimezoneOffset();
            }
            log.debug("ClientContext property '{}' not found in ClientContext map.", name);
            return null;
        }

        return props.get(name);
    }

    /**
     * Removes the given property from the ClientContext
     * @param name The name of the property to remove.
     */
    public void remove(String name) {
        Map<String, String> props = propertiesMap.get();
        if (props != null && name != null) {
            props.remove(name);
        }
    }

    public Set<String> getPropertyNames() {
        Map<String, String> props = propertiesMap.get();

        if (props == null) {
            return new HashSet<>();
        }

        return props.keySet();
    }

    public void clear() {
        propertiesMap.set(null);
    }
}
