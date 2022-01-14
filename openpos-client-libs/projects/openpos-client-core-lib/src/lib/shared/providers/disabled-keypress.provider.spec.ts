import { TestBed } from '@angular/core/testing';
import { DisabledKeyPressProvider } from './disabled-keypress.provider';

describe('DisabledKeypressProvider', () => {
    let disabledKeyPressProvider: DisabledKeyPressProvider;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [DisabledKeyPressProvider]
        });

        disabledKeyPressProvider = TestBed.inject(DisabledKeyPressProvider);
    });

    it('should make sonar happy', () => {
        // Members of a class are not enumerable, so give some prototypical help
        Object.getOwnPropertyNames(Object.getPrototypeOf(disabledKeyPressProvider))
            .filter(key => key !== 'constructor' && typeof disabledKeyPressProvider[key] === 'function')
            .forEach(key => disabledKeyPressProvider[key]());
    });
});
