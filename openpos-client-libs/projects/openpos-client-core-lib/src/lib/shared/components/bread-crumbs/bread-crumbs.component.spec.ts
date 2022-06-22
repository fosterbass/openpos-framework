import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionService } from '../../../core/actions/action.service';
import { BreadCrumbsComponent } from './bread-crumbs.component';

describe('BreadCrumbsComponent', () => {
    let component: BreadCrumbsComponent;
    let fixture: ComponentFixture<BreadCrumbsComponent>;
    let actionService: ActionService;

    const mockService = {
        doAction: () => { }
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [BreadCrumbsComponent],
            providers: [{ provide: ActionService, useValue: mockService }]
        }).compileComponents();
        fixture = TestBed.createComponent(BreadCrumbsComponent);
        component = fixture.componentInstance;
        actionService = TestBed.inject(ActionService);
        fixture.detectChanges();
    });

    it('renders', () => {
        expect(component).toBeDefined();
    });
    it('has a button for each action', () => {
        const firstAction = { action: 'first', title: 'First Title' };
        const secondAction = { action: 'second', title: 'Second Title' };
        component.crumbs = [firstAction, secondAction];
        fixture.detectChanges();

        const firstButton = fixture.nativeElement.querySelector('#breadCrumb_' + firstAction.action);
        expect(firstButton).not.toBeNull();
        expect(firstButton.innerHTML.trim()).toBe(firstAction.title);

        const secondButton = fixture.nativeElement.querySelector('#breadCrumb_' + secondAction.action);
        expect(secondButton).not.toBeNull();
        expect(secondButton.innerHTML.trim()).toBe(secondAction.title);
    });
    it('calls doAction when the button is clicked', () => {
        spyOn(actionService, 'doAction').and.callThrough();
        const action = { action: 'action' };
        component.crumbs = [action];
        fixture.detectChanges();

        const button = fixture.nativeElement.querySelector('#breadCrumb_action');
        button.click();
        expect(actionService.doAction).toHaveBeenCalledWith(action);
    });
});
