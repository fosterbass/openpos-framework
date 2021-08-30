import { PanEvent } from './pan-event';

export interface ScreenGestureInterface {
    action: string;
    swipes: string[];
    pans: PanEvent[];
    swipeTimeout?: number;
}