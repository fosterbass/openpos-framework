import { OpenposMessage } from './message';
import { MessageTypes } from './message-types';

export class ActionMessage implements OpenposMessage {
    type = MessageTypes.ACTION;
    lastKnownQueueSize: number
    constructor(public actionName: string, public doNotBlockForResponse: boolean, public payload?: any) { }
}
