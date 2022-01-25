package org.jumpmind.pos.core.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.server.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class FirebasePushNotificationManager {

    @Autowired
    IMessageService messageService;

    protected FirebaseApp firebaseApp;

    protected String registrationToken;

    private Object lock = new Object();

    @PostConstruct
    protected void init() {
        try {
            InputStream is = FirebasePushNotificationManager.class.getResourceAsStream("/push/jumpmind-firebase-token.json");
            if (is != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(is)).build();
                firebaseApp = FirebaseApp.initializeApp(options);
            } else {
                log.info("Unable to find google firebase access token. Will not be able to send push notifications.");
            }
        } catch (IOException e) {
            log.info("Unable to find google firebase access token. Will not be able to send push notifications.");
        }
    }

    public String registerDeviceForNotifications(String deviceId) {
        this.registrationToken = "";
        try {
            synchronized (this.lock) {
                Thread messageThread = new Thread(() ->
                    messageService.sendMessage(deviceId, new PushNotificationRegisterMessage())
                );
                messageThread.start();
                this.lock.wait(10000);
                messageThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return this.registrationToken;
    }

    public void saveDevice(String registrationToken) {
        synchronized (this.lock) {
            this.registrationToken = registrationToken;
            this.lock.notifyAll();
        }
    }

    public void sendPushNotification(String registrationToken, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setToken(registrationToken)
                .build();
        String response = FirebaseMessaging.getInstance().send(message);
        log.info("Sent Firebase message: " + response);
    }

}
