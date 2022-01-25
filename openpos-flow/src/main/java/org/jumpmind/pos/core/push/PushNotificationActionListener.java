package org.jumpmind.pos.core.push;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.server.model.Action;
import org.jumpmind.pos.server.service.IActionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Component
public class PushNotificationActionListener implements IActionListener {

    @Autowired
    FirebasePushNotificationManager firebasePushNotificationManager;

    @Override
    public Collection<String> getRegisteredTypes() {
        return Collections.singletonList("PushNotification");
    }

    @Override
    public void actionOccurred(String deviceId, Action action) {
        onPushNotificationRegistered(action);
    }

    public void onPushNotificationRegistered(Action action) {
        firebasePushNotificationManager.saveDevice(action.getData());
    }
}
