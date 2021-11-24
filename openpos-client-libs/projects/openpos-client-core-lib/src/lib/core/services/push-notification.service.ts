import { Injectable } from '@angular/core';
import { ActionPerformed, PushNotificationSchema, PushNotifications, Token } from '@capacitor/push-notifications';
import { MessageTypes } from '../messages/message-types';
import { SessionService } from './session.service';

@Injectable({
    providedIn: 'root',
})
export class PushNotificationService {

    constructor(
        private sessionService: SessionService
    ) {
        console.warn('Starting Push Notification Service...');
        this.sessionService.getMessages(MessageTypes.PUSH_REGISTER)
        .subscribe(m => {
            this.register();
        });
    }

    private register() {
        console.log('Registering for notifications...');
        // Request permission to use push notifications
        // iOS will prompt user and return if they granted permission or not
        // Android will just grant without prompting
        PushNotifications.requestPermissions().then(result => {
          if (result.receive === 'granted') {
            // Register with Apple / Google to receive push via APNS/FCM
            PushNotifications.register();
          } else {
            console.log('Push permissions were denied.');
          }
        });

        // On success, we should be able to receive notifications
        PushNotifications.addListener('registration',
          (token: Token) => {
            this.sessionService.publish('PushNotificationRegistered', 'PushNotification', token.value);
            console.log('Push registration success, token: ' + token.value);
          }
        );

        // Some issue with our setup and push will not work
        PushNotifications.addListener('registrationError',
          (error: any) => {
            console.log('Error on push registration: ' + JSON.stringify(error));
          }
        );

        // Show us the notification payload if the app is open on our device
        PushNotifications.addListener('pushNotificationReceived',
          (notification: PushNotificationSchema) => {
            console.log('Push received: ' + JSON.stringify(notification));
          }
        );

        // Method called when tapping on a notification
        PushNotifications.addListener('pushNotificationActionPerformed',
          (notification: ActionPerformed) => {
            console.log('Push action performed: ' + JSON.stringify(notification));
          }
        );
    }
}
