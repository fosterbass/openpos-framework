import { Injectable, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { LoaderState } from '../../shared/components/loader/loader-state';
import { MessageProvider } from '../../shared/providers/message.provider';
import { ConfirmationDialogComponent } from '../components/confirmation-dialog/confirmation-dialog.component';
import { ActionMessage } from '../messages/action-message';
import { OpenposMessage } from '../messages/message';
import { MessageTypes } from '../messages/message-types';
import { QueueLoadingMessage } from '../services/session.service';
import { IActionItem } from './action-item.interface';
import { IUrlMenuItem } from './url-menu-item.interface';

@Injectable()
export class ActionService implements OnDestroy {

    private blockActions: boolean;
    private actionPayloads: Map<string, () => void> = new Map<string, () => void>();
    private actionDisablers = new Map<string, BehaviorSubject<boolean>>();
    private subscriptions = new Subscription();
    private actionQueue = new Array<QueuedItem>();

    constructor(
        private dialogService: MatDialog,
        private messageProvider: MessageProvider) {
        console.log('Creating new Action Service');
        this.subscriptions.add(messageProvider.getScopedMessages$().subscribe(message => {
            if (message.willUnblock === false) {
                console.log('creating a screen that is disabled');
                this.blockActions = true;
            }
            // else if (message.willUnblock) {
            //     console.log('unblocking actions because message:', message);
            //     this.unblock();
            // }
        }));
        this.subscriptions.add(messageProvider.getAllMessages$<OpenposMessage>().subscribe(message => {
            if ((message.type === MessageTypes.TOAST ||
                    message.type === MessageTypes.SCREEN) && message.willUnblock) {
                console.log('unblocking action because toast:', message);
                this.unblock();
            }
        }));
    }

    async doAction(actionItem: IActionItem, payload?: any) {
        const sendAction = await this.canPerformAction(actionItem);
        if (sendAction) {
            if (typeof payload !== 'undefined' && typeof actionItem.defaultPayload !== 'undefined') {
                console.warn('Default action payload overridden for action ' + actionItem.action);
            }

            if (typeof payload === 'undefined' && typeof actionItem.defaultPayload !== 'undefined') {
                payload = actionItem.defaultPayload;
            }

            if (!actionItem.doNotBlockForResponse) {
                this.blockActions = true;
            }
            if (actionItem.hasOwnProperty('url')) {
                this.doUrlAction(actionItem as IUrlMenuItem);
                return;
            }

            // First we will use the payload passed into this function then
            // Check if we have registered action payload
            if (!payload && this.actionPayloads.has(actionItem.action)) {
                console.info(`Checking registered action payload for ${actionItem.action}`);
                try {
                    payload = this.actionPayloads.get(actionItem.action)();
                } catch (e) {
                    throw new Error(`invalid action payload for ${actionItem.action}: ` + e);
                }
            }

            let actionMessage = new ActionMessage(actionItem.action, actionItem.doNotBlockForResponse, payload);
            console.log("action queue size is " + this.actionQueue.length);
            actionMessage.lastKnownQueueSize = this.actionQueue.length;
            this.messageProvider.sendMessage(actionMessage);
            if (!actionItem.doNotBlockForResponse) {
                this.queueLoading();
            }
        } else if (actionItem.queueIfBlocked) {
            console.log('queueing an action to send');
            this.actionQueue.push(new QueuedItem(actionItem, payload));
        }
    }

    public actionBlocked(): boolean {
        return this.blockActions;
    }

    public registerActionPayload(actionName: string, actionValue: () => void) {
        this.actionPayloads.set(actionName, actionValue);
    }

    public unregisterActionPayloads() {
        this.actionPayloads.clear();
    }

    public unregisterActionPayload(actionName: string) {
        this.actionPayloads.delete(actionName);
    }

    public registerActionDisabler(action: string, disabler: Observable<boolean>): Subscription {
        if (!this.actionDisablers.has(action)) {
            this.actionDisablers.set(action, new BehaviorSubject<boolean>(false));
        }

        return disabler.subscribe(value => this.actionDisablers.get(action).next(value));
    }

    public actionIsDisabled$(action: string): Observable<boolean> {
        if (!this.actionDisablers.has(action)) {
            this.actionDisablers.set(action, new BehaviorSubject<boolean>(false));
        }

        return this.actionDisablers.get(action);
    }

    public actionIsDisabled(action: string): boolean {
        if (!this.actionDisablers.has(action)) {
            return false;
        }

        return this.actionDisablers.get(action).value;
    }

    ngOnDestroy(): void {
        if (this.subscriptions) {
            this.subscriptions.unsubscribe();
        }
    }

    private unblock() {
        this.blockActions = false;
        const queued = this.actionQueue.pop();
        if (queued) {
            console.log('Dequeued an action to send');
            this.doAction(queued.item, queued.payload);
        }
    }

    private doUrlAction(urlItem: IUrlMenuItem) {
        // check to see if we are an IURLMenuItem
        console.info(`About to open: ${urlItem.url} in target mode: ${urlItem.targetMode}, with options: ${urlItem.options}`);
        window.open(urlItem.url, urlItem.targetMode, urlItem.options);
    }

    private queueLoading() {
        this.messageProvider.sendMessage(new QueueLoadingMessage(LoaderState.LOADING_TITLE));
    }

    private async canPerformAction(actionItem: IActionItem): Promise<boolean> {
        if (actionItem && typeof actionItem.enabled === 'boolean' && actionItem.enabled === false) {
            console.info('Not sending action because it was disabled');
            return false;
        }

        if (this.blockActions) {
            console.info('Not sending action because previous action required a response that we are still waiting for');
            return false;
        }

        if (this.actionIsDisabled(actionItem.action)) {
            console.info('Not sending action because it was disabled by a disabler');
            return false;
        }

        if (actionItem.confirmationDialog) {
            console.info('Confirming action');
            const dialogRef = this.dialogService.open(ConfirmationDialogComponent, { disableClose: true });
            dialogRef.componentInstance.confirmDialog = actionItem.confirmationDialog;
            const result = await dialogRef.afterClosed().toPromise();

            // if we didn't confirm return and don't send the action to the server
            if (!result) {
                console.info('Canceling action because confirmation was negative');
                return false;
            }
        }

        return true;
    }
}

export class QueuedItem {
    constructor(public item: IActionItem, public payload: any) {
    }
}
