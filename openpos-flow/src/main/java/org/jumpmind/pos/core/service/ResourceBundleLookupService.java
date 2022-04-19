package org.jumpmind.pos.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Component
public class ResourceBundleLookupService implements IResourceLookupService{

	@Override
	public String getString(String deviceId, String group, String key) {
        String value = null;
        ResourceBundle screenProperties = ResourceBundle.getBundle("i18n/" + group);
        try {
            value = screenProperties.getString(key);
        } catch (MissingResourceException ex) {
            // allow failure for key as given, then try lower case version of key
            // below. prior behavior was to only try lower case version of key
        }
        if (value == null) {
            value = screenProperties.getString(key.toLowerCase());
        }

		return value;
	}

}
