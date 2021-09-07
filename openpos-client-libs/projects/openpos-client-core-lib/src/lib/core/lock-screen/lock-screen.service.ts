import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal, PortalInjector } from '@angular/cdk/portal';
import { Injectable, Injector } from '@angular/core';
import { BehaviorSubject, ReplaySubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { FocusService } from '../focus/focus.service';
import { LockScreenMessage } from '../messages/lock-screen-message';
import { MessageTypes } from '../messages/message-types';
import { SessionService } from '../services/session.service';
import { LockScreenComponent } from './lock-screen.component';
import { LOCK_SCREEN_DATA } from './lock-screen-data';

@Injectable({
    providedIn: 'root'
})
export class LockScreenService {
    private lockScreenOverlayRef: OverlayRef;
    private lockScreenData = new ReplaySubject<LockScreenMessage>();
    public enabled$ = new BehaviorSubject(false);

    constructor(sessionService: SessionService,
        private overlay: Overlay,
        private injector: Injector,
        private focusService: FocusService
    ) {
        sessionService.getMessages(MessageTypes.LOCK_SCREEN).pipe(
            tap(() => this.enabled$.next(true)),
            tap(() => this.showLockScreen()),
            tap(message => this.lockScreenData.next(message))
        ).subscribe();
        sessionService.getMessages(MessageTypes.UNLOCK_SCREEN).pipe(
            tap(() => this.enabled$.next(false)),
            tap(message => this.removeLockScreen(message))
        ).subscribe();
    }

    private showLockScreen() {
        if (this.lockScreenOverlayRef != null) return;

        this.lockScreenOverlayRef = this.overlay.create({
            height: '100%',
            width: '100%',
            panelClass: 'lockscreen-overlay'
        });
        const lockScreenPortal = new ComponentPortal(LockScreenComponent, null, this.createInjector());
        this.lockScreenOverlayRef.attach(lockScreenPortal);
        this.focusService.createInitialFocus(this.lockScreenOverlayRef.hostElement)
    }

    private removeLockScreen(message: LockScreenMessage) {
        if (this.lockScreenOverlayRef) {
            this.lockScreenOverlayRef.dispose();
            this.lockScreenOverlayRef = null;
        }

    }

    private createInjector(): PortalInjector {
        // Instantiate new WeakMap for our custom injection tokens
        const injectionTokens = new WeakMap();

        // Set custom injection tokens
        injectionTokens.set(LOCK_SCREEN_DATA, this.lockScreenData);
        // Instantiate new PortalInjector
        return new PortalInjector(this.injector, injectionTokens);
    }

}
