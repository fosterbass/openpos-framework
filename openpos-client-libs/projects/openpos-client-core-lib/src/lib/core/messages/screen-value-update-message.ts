import { OpenposMessage } from './message';
import { MessageTypes } from './message-types';

export class ScreenValueUpdateMessage<T> implements OpenposMessage {
    type = MessageTypes.SCREEN_VALUE_UPDATE;
    valuePath: string;
    value: T;
}
