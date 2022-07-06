package org.jumpmind.pos.core.screeninterceptor;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.core.service.IKeyMappingService;
import org.jumpmind.pos.core.service.LocaleType;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
// ProxyMode so this becomes a DeviceScope bean when being @Autowired into a
// list of interfaces
@Scope(proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS, value = "device")
public class KeyMappingScreenPropertyStrategy implements IMessagePropertyStrategy<UIMessage> {
    @Autowired(required = false)
    private IKeyMappingService keyMappingService;

    @Autowired
    private ClientContext clientContext;

    @Override
    public Object doStrategy(
            String deviceId,
            Object property,
            Class<?> clazz,
            UIMessage screen,
            Map<String, Object> screenContext) {
        if (property != null && ActionItem.class.isAssignableFrom(clazz) && keyMappingService != null) {
            ActionItem item = (ActionItem) property;
            if (item.isAutoAssignEnabled()) {
                String keyMapping = keyMappingService.getKeyMapping(screen, item.getAction(), screenContext);
                if (!StringUtils.isEmpty(keyMapping)) {
                    item.setKeybind(keyMapping);
                    String keyMappingDisplayName = keyMappingService.getDisplayName(clientContext.get(ClientContext.DISPLAY_LOCALE), screen.getId(), keyMapping);
                    item.setKeybindDisplayName(keyMappingDisplayName);
                }
            }
        }
        return property;
    }
}
