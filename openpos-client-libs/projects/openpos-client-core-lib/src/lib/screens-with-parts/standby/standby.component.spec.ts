import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MockComponent, MockPipe } from 'ng-mocks';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { IconButtonComponent } from '../../shared/components/icon-button/icon-button.component';
import { ScreenGestureComponent } from '../../shared/screen-parts/screen-gesture/screen-gesture.component';
import { StandbyComponent } from './standby.component';
import { ActionService } from '../../core/actions/action.service';
import { BackgroundImageUrlPipe } from '../../shared/pipes/background-image-url.pipe';
import { StandByInterface } from './standby.interface';

describe('StandbyComponent', () => {
    let standbyComponent: StandbyComponent;
    let fixture: ComponentFixture<StandbyComponent>;

    const mockService = {
        doAction: () => { }
    };

    const mockScreenData: StandByInterface = {
        screenType: 'Standby',
        type: 'Screen',
        backgroundImage: 'imageUrl'
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [
                StandbyComponent,
                IconButtonComponent,
                MockComponent(IconComponent),
                MockComponent(ScreenGestureComponent),
                MockPipe(BackgroundImageUrlPipe)
            ],
            providers: [{ provide: ActionService, useValue: mockService }],
        }).compileComponents();

        fixture = TestBed.createComponent(StandbyComponent);
        standbyComponent = fixture.componentInstance;
        standbyComponent.screen = JSON.parse(JSON.stringify(mockScreenData));
        fixture.detectChanges();
    });

    it('creates', () => {
        expect(standbyComponent).toBeTruthy();
    });

    describe('unlock icon button', () => {
        it('does not have an icon button by default', () => {
            expect(fixture.nativeElement.querySelector('app-icon-button')).toBeFalsy();
        });
        it('has an icon button when an icon is specified in the screen data', () => {
            standbyComponent.screen.iconName = 'someIcon';
            fixture.detectChanges();
            expect(fixture.nativeElement.querySelector('app-icon-button')).toBeTruthy();
        });
    });

    describe('iconClicked()', () => {
        it('does the action', () => {
            spyOn(standbyComponent.actionService, 'doAction').and.callThrough();
            standbyComponent.screen.iconAction = 'someAction';
            standbyComponent.screen.iconName = 'someIcon';
            fixture.detectChanges();
            fixture.nativeElement.querySelector('button').click();
            expect(standbyComponent.actionService.doAction).toHaveBeenCalledWith({ action: 'someAction' }, undefined);
        });
    });
});
