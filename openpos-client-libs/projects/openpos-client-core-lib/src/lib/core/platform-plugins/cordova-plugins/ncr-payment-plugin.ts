import { IPlatformPlugin } from '../platform-plugin.interface';
import { Observable, Subscriber } from 'rxjs';
import { filter } from 'rxjs/operators';
import { SessionService } from '../../services/session.service';
import { Injectable } from '@angular/core';
import { ActionMessage } from '../../messages/action-message';
import { MessageTypes } from '../../messages/message-types';

@Injectable({
    providedIn: 'root'
})
export class NCRPaymentPlugin implements IPlatformPlugin {

    private ncrCordovaPlugin;

    constructor(private sessionService: SessionService) { }

    name(): string {
        return 'NCRPaymentPlugin';
    }

    pluginPresent(): boolean {
        return !!window.hasOwnProperty('NCRCordovaPlugin');
    }

    initialize(): Observable<string> {
        return new Observable((initialized: Subscriber<string>) => {
            // tslint:disable-next-line:no-string-literal
            this.ncrCordovaPlugin = window['NCRCordovaPlugin'];
            if (!this.ncrCordovaPlugin) {
                initialized.error(`Tried to initialize plugin ${this.name()} which is not present`);
            }

            this.sessionService.getMessages('Proxy').pipe(
                filter(m => m.proxyType === 'Payment')
            ).subscribe(message => {
                this.forwardMessage(message);
            });

            initialized.next(`Successfully intitialized ${this.name()}`);
            initialized.complete();
        });
    }

    forwardMessage(message: any) {
        if (message.action === 'ProcessMessage') {
            this.ncrCordovaPlugin.processMessage(message.payload,
                response => { this.handleSuccess(message, response); },
                response => { this.handleError(message, response); }
            );
        }
    }

    handleSuccess(message: any, response: string) {
        const responseMessage = new ActionMessage('response', true, { messageId: message.messageId, payload: response, success: true });
        responseMessage.type = MessageTypes.PROXY;
        this.sessionService.sendMessage(responseMessage);
        console.log('SUCCESSFUL RESPONSE: ' + response);
    }

    handleError(message: any, response: string) {
        const responseMessage = new ActionMessage('response', true, { messageId: message.messageId, payload: response, success: false });
        responseMessage.type = MessageTypes.PROXY;
        this.sessionService.sendMessage(responseMessage);
        console.log('ERROR RESPONSE: ' + response);
    }

}
