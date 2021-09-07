import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ElectronService } from 'ngx-electron';
import { ActionService } from '../../../core/actions/action.service';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { KeyPressProvider } from '../../providers/keypress.provider';
import { PanEvent } from './pan-event';
import { ScreenGestureComponent } from './screen-gesture.component';
import { SwipeEvent } from './swipe-event';

describe('ScreenGestureComponent', () => {
    let component: ScreenGestureComponent;
    let fixture: ComponentFixture<ScreenGestureComponent>;

    const mockService = { doAction: () => { } };
    class ClientContext { };
    const action = 'Back';
    const unlockPans: PanEvent[] = [{ angleLower: -90, angleUpper: 0, distance: 500 }]
    const unlockSwipes = [SwipeEvent.SWIPE_UP, SwipeEvent.SWIPE_DOWN, SwipeEvent.SWIPE_LEFT, SwipeEvent.SWIPE_RIGHT];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            declarations: [ScreenGestureComponent],
            providers: [
                { provide: MatDialog, useValue: mockService },
                { provide: ActionService, useValue: mockService },
                { provide: ElectronService, useValue: mockService },
                { provide: KeyPressProvider, useValue: mockService },
                { provide: ClientContext, useValue: {} },
                { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
            ]
        }).compileComponents();
        fixture = TestBed.createComponent(ScreenGestureComponent);
        component = fixture.componentInstance;
        component.screenData = { action, pans: unlockPans, swipes: unlockSwipes, swipeTimeout: 200 };
        fixture.detectChanges();
    });

    it('renders', () => {
        expect(component).toBeDefined();
    });

    describe('ngOnInit', () => {
        beforeEach(() => {
            expect(component.hasUnlockPans).toBeTruthy();
            expect(component.hasUnlockSwipes).toBeTruthy();
            expect(component.swipeTimeoutMillis).toBe(200);
        })
        it('updates the flags when screenData is undefined', () => {
            delete component.screenData;
            component.ngOnInit();
            expect(component.hasUnlockPans).toBeFalsy();
            expect(component.hasUnlockSwipes).toBeFalsy();
            expect(component.swipeTimeoutMillis).toBe(200);
        });
        it('updates the flags when pans and swipes are undefined', () => {
            delete component.screenData.pans;
            delete component.screenData.swipes;
            component.ngOnInit();
            expect(component.hasUnlockPans).toBeFalsy();
            expect(component.hasUnlockSwipes).toBeFalsy();
            expect(component.swipeTimeoutMillis).toBe(200);
        });
        it('updates the flags and timeout when pans and swipes are empty', () => {
            component.screenData.pans = [];
            component.screenData.swipes = [];
            component.screenData.swipeTimeout = 100;
            component.ngOnInit();
            expect(component.hasUnlockPans).toBeFalsy();
            expect(component.hasUnlockSwipes).toBeFalsy();
            expect(component.swipeTimeoutMillis).toBe(100);
        });
    });

    describe('screenDataUpdated', () => {
        beforeEach(() => {
            expect(component.hasUnlockPans).toBeTruthy();
            expect(component.hasUnlockSwipes).toBeTruthy();
            expect(component.swipeTimeoutMillis).toBe(200);
        })
        it('updates the flags when screenData is undefined', () => {
            delete component.screenData;
            component.screenDataUpdated();
            expect(component.hasUnlockPans).toBeFalsy();
            expect(component.hasUnlockSwipes).toBeFalsy();
            expect(component.swipeTimeoutMillis).toBe(200);
        });
        it('updates the flags when pans and swipes are undefined', () => {
            delete component.screenData.pans;
            delete component.screenData.swipes;
            component.screenDataUpdated();
            expect(component.hasUnlockPans).toBeFalsy();
            expect(component.hasUnlockSwipes).toBeFalsy();
            expect(component.swipeTimeoutMillis).toBe(200);
        });
        it('updates the flags and timeout when pans and swipes are empty', () => {
            component.screenData.pans = [];
            component.screenData.swipes = [];
            component.screenData.swipeTimeout = 100;
            component.screenDataUpdated();
            expect(component.hasUnlockPans).toBeFalsy();
            expect(component.hasUnlockSwipes).toBeFalsy();
            expect(component.swipeTimeoutMillis).toBe(100);
        });
    });

    describe('unlock by swipe', () => {
        it('triggers the provided action when the swipes are complete in the correct order', () => {
            spyOn(component.actionService, 'doAction').and.callThrough();
            component.onSwipeUp();
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onSwipeDown();
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onSwipeLeft();
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onSwipeRight();
            expect(component.actionService.doAction).toHaveBeenCalledWith({ action }, undefined);
        });
        it('does not trigger an action when the swipes are completed in the wrong order', () => {
            spyOn(component.actionService, 'doAction').and.callThrough();
            component.onSwipeDown();
            component.onSwipeUp();
            component.onSwipeRight();
            component.onSwipeLeft();
            expect(component.actionService.doAction).not.toHaveBeenCalled();
        });
        it('clears swipes after timeout', (done: DoneFn) => {
            component.onSwipeUp();
            component.onSwipeDown();
            expect(component.swipeEvents).toEqual([SwipeEvent.SWIPE_UP, SwipeEvent.SWIPE_DOWN]);
            setTimeout(() => {
                expect(component.swipeEvents).toEqual([]);
                done();
            }, component.swipeTimeoutMillis);
        });
    });

    describe('unlock by pan', () => {
        beforeEach(() => {
            spyOn(component.actionService, 'doAction').and.callThrough();
        })
        it('triggers the provided action when the pan are complete', () => {
            component.onPanStart({ angle: -45, distance: 5 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: -40, distance: 100 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: -60, distance: 300 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanEnd({ angle: -50, distance: 500 });
            expect(component.actionService.doAction).toHaveBeenCalledWith({ action }, undefined);
        });
        it('does not trigger the action if any part of the pan is outside the bounds', () => {
            component.onPanStart({ angle: -45, distance: 5 });
            component.onPanMove({ angle: 1, distance: 100 });
            component.onPanMove({ angle: -60, distance: 300 });
            component.onPanEnd({ angle: -50, distance: 500 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
        });
        it('triggers the provided action only when all pans are complete', () => {
            component.screenData.pans = [{ angleLower: -90, angleUpper: 0, distance: 500 }, { angleLower: 0, angleUpper: 90, distance: 500 }];
            component.onPanStart({ angle: -45, distance: 5 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: -40, distance: 100 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: -60, distance: 500 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: 45, distance: 100 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: 60, distance: 300 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanEnd({ angle: 30, distance: 500 });
            expect(component.actionService.doAction).toHaveBeenCalledWith({ action }, undefined);
        });
        it('allows the first pan to go beyond the distance before completing the second pan', () => {
            component.screenData.pans = [{ angleLower: -90, angleUpper: 0, distance: 500 }, { angleLower: 0, angleUpper: 90, distance: 500 }];
            component.onPanStart({ angle: -45, distance: 5 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: -40, distance: 400 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: -60, distance: 700 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: 45, distance: 100 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanMove({ angle: 60, distance: 300 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanEnd({ angle: 30, distance: 600 });
            expect(component.actionService.doAction).toHaveBeenCalledWith({ action }, undefined);
        });
        it('does not trigger the action if any part of the second pan is outside the bounds', () => {
            component.screenData.pans = [{ angleLower: -90, angleUpper: 0, distance: 500 }, { angleLower: 0, angleUpper: 90, distance: 500 }];
            component.onPanStart({ angle: -45, distance: 5 });
            component.onPanMove({ angle: -60, distance: 300 });
            component.onPanEnd({ angle: -50, distance: 500 });
            component.onPanMove({ angle: 45, distance: 100 });
            component.onPanMove({ angle: 91, distance: 300 });
            component.onPanEnd({ angle: 30, distance: 600 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
        });
        it('if a pan is messed up there is no timer to restart it', () => {
            component.onPanStart({ angle: -45, distance: 5 });
            component.onPanMove({ angle: 1, distance: 100 });
            component.onPanEnd({ angle: -50, distance: 500 });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
            component.onPanStart({ angle: -45, distance: 5 });
            component.onPanMove({ angle: -60, distance: 100 });
            component.onPanEnd({ angle: -50, distance: 500 });
            expect(component.actionService.doAction).toHaveBeenCalledWith({ action }, undefined);
        });
    });
});
