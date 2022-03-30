import {MessageTypes} from './message-types';
import {OpenposMessage} from './message';

export class PlccApplyMessage implements OpenposMessage {
    type = MessageTypes.PLCC_APPLY;
    isActiveOnCustomerDisplay: boolean;
    detailsMessage: string;
}
