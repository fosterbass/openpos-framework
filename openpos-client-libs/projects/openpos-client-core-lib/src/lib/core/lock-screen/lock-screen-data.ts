import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { LockScreenMessage } from '../messages/lock-screen-message';

export const LOCK_SCREEN_DATA = new InjectionToken<Observable<LockScreenMessage>>('LOCK_SCREEN_DATA');
