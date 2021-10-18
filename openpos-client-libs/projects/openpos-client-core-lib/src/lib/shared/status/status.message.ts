import { OpenposMessage } from '../../core/messages/message';
import { MessageTypes } from '../../core/messages/message-types';
import { Status } from '../../core/messages/status.enum';

export class StatusMessage implements OpenposMessage {
    type = MessageTypes.STATUS;
    constructor(
        public id: string,
        public name: string,
        public icon: string,
        public status: Status,
        public message: string
    ) { }

}
