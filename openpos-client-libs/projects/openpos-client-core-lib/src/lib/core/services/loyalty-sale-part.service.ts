import { Injectable, OnDestroy } from '@angular/core';
import { MessageTypes } from '../messages/message-types';
import { takeUntil } from 'rxjs/operators';
import { LoyaltySignupMessage } from '../messages/loyalty-signup-message';
import { SessionService } from './session.service';
import { BehaviorSubject, Subject, Observable } from 'rxjs';
import { PlccApplyMessage } from '../messages/plcc-apply-message';

@Injectable({
    providedIn: 'root',
})
export class LoyaltySalePartService implements OnDestroy {
    private destroyed$ = new Subject();
    private isActiveOnCustomerDisplay$ = new BehaviorSubject(false);
    private detailsMessage$ = new BehaviorSubject('');
    private isLoyaltyActive: boolean;
    private loyaltyMessage: string;
    private isPLCCActive: boolean;
    private plccMessage: string;

    constructor(private sessionService: SessionService) {
        this.sessionService.getMessages(MessageTypes.LOYALTY_SIGNUP, MessageTypes.PLCC_APPLY)
            .pipe(takeUntil(this.destroyed$))
            .subscribe((message: LoyaltySignupMessage | PlccApplyMessage) => {
                if (message.type === MessageTypes.LOYALTY_SIGNUP) {
                    this.isLoyaltyActive = message.isActiveOnCustomerDisplay;
                    this.loyaltyMessage = message.detailsMessage;
                } else if (message.type === MessageTypes.PLCC_APPLY) {
                    this.isPLCCActive = message.isActiveOnCustomerDisplay;
                    this.plccMessage = message.detailsMessage;
                }
                this.isActiveOnCustomerDisplay$.next(this.isLoyaltyActive || this.isPLCCActive);
                this.detailsMessage$.next(this.isLoyaltyActive ? this.loyaltyMessage : this.plccMessage);
            });
    }

    checkCustomerDisplayStatus(): void {
        this.sessionService.publish('GetStatus', 'CustomerDisplayLoyaltySignup');
        this.sessionService.publish('GetStatus', 'CustomerDisplayPLCCApply');
    }

    isActiveOnCustomerDisplay(): Observable<boolean> {
        return this.isActiveOnCustomerDisplay$.asObservable();
    }

    getCustomerDisplayDetailsMessage(): Observable<string> {
        return this.detailsMessage$.asObservable();
    }

    ngOnDestroy() {
        this.destroyed$.next();
    }
}


