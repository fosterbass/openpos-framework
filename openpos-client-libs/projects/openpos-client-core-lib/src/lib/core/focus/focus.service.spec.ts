import { FocusService } from './focus.service';
import { ConfigurableFocusTrapFactory, ConfigurableFocusTrap } from '@angular/cdk/a11y';
import { TestBed, fakeAsync } from '@angular/core/testing';

describe('FocusService', () => {

    let focusService: FocusService;
    let focusTrap: jasmine.SpyObj<ConfigurableFocusTrap>;
    const focusTrapFactory = {
        create: (elm) => focusTrap
    };
    let htmlElement: jasmine.SpyObj<HTMLElement>;

    beforeEach(() => {
        focusTrap = jasmine.createSpyObj('ConfigurableFocusTrap', ['focusInitialElementWhenReady', 'destroy', 'focusInitialElement']);
        htmlElement = jasmine.createSpyObj('HTMLElement', ['focus']);

        TestBed.configureTestingModule({
            providers: [
                { provide: ConfigurableFocusTrapFactory, useValue: focusTrapFactory },
                FocusService
            ]
        });

        focusService = TestBed.inject(FocusService);
    });

    describe('createInitialFocus', () => {
        it('sets up focusTrap', () => {
            spyOn(focusTrapFactory, 'create').and.callThrough();
            focusService.createInitialFocus(htmlElement);
            expect(focusTrapFactory.create).toHaveBeenCalledWith(htmlElement);
            expect(focusTrap.focusInitialElementWhenReady).toHaveBeenCalled();
        });
    });

    describe('destroy', () => {
        it('will not destory focusTrap if it has not been initialized', () => {
            focusService.destroy();
            expect(focusTrap.destroy).not.toHaveBeenCalled();
        });

        it('will destory focusTrap, after it has been initialized', () => {
            focusService.createInitialFocus(htmlElement);
            focusService.destroy();
            expect(focusTrap.destroy).toHaveBeenCalled();
        });
    });

    describe('restoreInitialFocus', () => {
        it('will not restore initial focus if it has not been initialized', () => {
            focusService.restoreInitialFocus();
            expect(focusTrap.focusInitialElement).not.toHaveBeenCalled();
        });

        it('will restore intiial focus if it has been initialized', () => {
            focusService.createInitialFocus(htmlElement);
            focusService.restoreInitialFocus();
            expect(focusTrap.focusInitialElement).toHaveBeenCalled();
        });

    });

    describe('restoreFocus', () => {
        it('will not restore existing focus if element is undefined', fakeAsync(() => {
            focusService.restoreFocus(undefined);
            jasmine.clock().tick(100);
            expect(htmlElement.focus).not.toHaveBeenCalled();
        }));

        it('will restore existing focus to element', fakeAsync(() => {
            focusService.restoreFocus(htmlElement);
            jasmine.clock().tick(100);
            expect(htmlElement.focus).toHaveBeenCalled();
        }));
    });
});
