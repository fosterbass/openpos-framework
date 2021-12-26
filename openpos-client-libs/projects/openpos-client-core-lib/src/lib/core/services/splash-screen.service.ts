import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface SplashScreenMessageRef {
    message: string;
    pop();
}

@Injectable({
    providedIn: 'root'
})
export class SplashScreen {
    private _message = new BehaviorSubject<string | null>(null);

    private _messageStack = new Array<SplashScreenMessageRef>();

    observeMessage(): Observable<string | null> {
        return this._message.asObservable();
    }

    pushMessage(message: string): SplashScreenMessageRef {
        const ref = {
            message,
            pop: () => {
                this._messageStack.splice(this._messageStack.indexOf(ref), 1);
                this._updateMessage();
            }
        };

        this._messageStack.unshift(ref);

        this._updateMessage();

        return ref;
    }

    private _updateMessage() {
        let newValue = null;

        if (this._messageStack.length >= 1) {
            newValue = this._messageStack[0].message;
        }

        if (this._message.value === newValue) {
            return;
        }

        this._message.next(newValue);
    }
}
