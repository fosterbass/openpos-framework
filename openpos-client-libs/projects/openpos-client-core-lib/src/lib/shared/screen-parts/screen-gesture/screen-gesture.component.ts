import { Component, Injector, OnInit } from '@angular/core';
import { SwipeEvent } from './swipe-event';
import { ScreenPart } from '../../decorators/screen-part.decorator';
import { ScreenPartComponent } from '../screen-part';
import { ScreenGestureInterface } from './screen-gesture.interface';
import 'hammerjs';

@ScreenPart({
    name: 'screenGesture'
})
@Component({
    selector: 'app-screen-gesture',
    templateUrl: './screen-gesture.component.html',
    styleUrls: ['./screen-gesture.component.scss']
})
export class ScreenGestureComponent extends ScreenPartComponent<ScreenGestureInterface> implements OnInit {
    hasUnlockSwipes = true;
    swipeEvents: SwipeEvent[] = [];
    swipeTimeout: NodeJS.Timer;
    swipeTimeoutMillis = 2000;

    hasUnlockPans = true;
    isPanValid: boolean;
    currentPanValidationIndex: number = 0;

    constructor(injector: Injector) {
        super(injector);
    }

    ngOnInit(): void {
        super.ngOnInit();
        this.updateFromScreenData();
    }

    screenDataUpdated(): void {
        this.updateFromScreenData();
    }

    private updateFromScreenData(): void {
        this.hasUnlockSwipes = this.screenData && this.screenData.swipes && this.screenData.swipes.length > 0;
        this.hasUnlockPans = this.screenData && this.screenData.pans && this.screenData.pans.length > 0;
        if (this.screenData && this.screenData.swipeTimeout) {
            this.swipeTimeoutMillis = this.screenData.swipeTimeout;
        }
    }

    onPanStart(event): void {
        this.isPanValid = true;
        this.currentPanValidationIndex = 0;
        this.checkForPanComplete(event.angle, event.distance);
    }

    onPanMove(event): void {
        this.checkForPanComplete(event.angle, event.distance);
    }

    onPanEnd(event): void {
        this.checkForPanComplete(event.angle, event.distance);
        this.isPanValid = false;
        this.currentPanValidationIndex = 0;
    }

    private checkForPanComplete(currentAngle: number, currentDistance: number): void {
        const currentPanValidationEvent = this.screenData.pans[this.currentPanValidationIndex];
        if (!this.isInCurrentBounds(currentAngle) && !this.isInPreviousBounds(currentAngle)) {
            this.isPanValid = false;
        }
        if (this.isPanValid && this.isInCurrentBounds(currentAngle) && currentDistance >= currentPanValidationEvent.distance) {
            if (this.currentPanValidationIndex < this.screenData.pans.length - 1) {
                this.currentPanValidationIndex++;
            } else {
                this.doAction(this.screenData.action);
            }
        }
    }

    private isInCurrentBounds(currentAngle: number): boolean {
        const currentPanValidationEvent = this.screenData.pans[this.currentPanValidationIndex];
        return currentAngle >= currentPanValidationEvent.angleLower && currentAngle <= currentPanValidationEvent.angleUpper;
    }

    private isInPreviousBounds(currentAngle: number): boolean {
        if (this.currentPanValidationIndex > 0) {
            const previousPanValidationEvent = this.screenData.pans[this.currentPanValidationIndex - 1];
            return currentAngle >= previousPanValidationEvent.angleLower && currentAngle <= previousPanValidationEvent.angleUpper;
        }
        return false;
    }

    onSwipeUp(): void {
        this.swipeEvents.push(SwipeEvent.SWIPE_UP);
        this.checkForSwipeComplete();
    }

    onSwipeDown(): void {
        this.swipeEvents.push(SwipeEvent.SWIPE_DOWN);
        this.checkForSwipeComplete();
    }

    onSwipeLeft(): void {
        this.swipeEvents.push(SwipeEvent.SWIPE_LEFT);
        this.checkForSwipeComplete();
    }

    onSwipeRight(): void {
        this.swipeEvents.push(SwipeEvent.SWIPE_RIGHT);
        this.checkForSwipeComplete();
    }

    private checkForSwipeComplete(): void {
        console.log('Checking if the current swipe sequence is complete.', this.swipeEvents);
        let successfulUnlock: boolean = false;
        if (this.swipeEvents.length == this.screenData.swipes.length) {
            successfulUnlock = true;
            for (let i = 0; i < this.screenData.swipes.length; i++) {
                if (this.screenData.swipes[i] != this.swipeEvents[i]) {
                    successfulUnlock = false;
                }
            }
            if (successfulUnlock) {
                this.doAction(this.screenData.action);
            }
        }
        if (!successfulUnlock) {
            clearTimeout(this.swipeTimeout);
            this.swipeTimeout = setTimeout(() => { this.swipeEvents = []; }, this.swipeTimeoutMillis);
        }
    }
}