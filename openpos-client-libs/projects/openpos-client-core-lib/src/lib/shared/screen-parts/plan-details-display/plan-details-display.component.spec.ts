import { validateExist, validateText } from '../../../utilites/test-utils';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActionService } from '../../../core/actions/action.service';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { PlanDetailsDisplayComponent } from './plan-details-display.component';
import { Plan } from '../../../screens-with-parts/membership/plan-interface';
import { ActionItem } from '../../../core/actions/action-item';
import { SafeHtmlPipe } from '../../pipes/safe-html.pipe';
import { MockComponent } from 'ng-mocks';
import { IconComponent } from '../../components/icon/icon.component';

class MockActionService { }
class MockMatDialog { }

describe('PlanDetailsDisplayComponent', () => {
    let component: PlanDetailsDisplayComponent;
    let fixture: ComponentFixture<PlanDetailsDisplayComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            declarations: [
                PlanDetailsDisplayComponent,
                SafeHtmlPipe,
                MockComponent(IconComponent)
            ],
            providers: [
                { provide: MatDialog, useClass: MockMatDialog },
                { provide: ActionService, useClass: MockActionService },
                { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
            ]
        }).compileComponents();
        fixture = TestBed.createComponent(PlanDetailsDisplayComponent);
        component = fixture.componentInstance;
        component.plan = {
            title: 'Silver Tier',
            iconImageUrl: 'code',
            copy: '<b>SILVER TIER</b><hr>Get the good stuff without the extra fancy fluff.',
            signupActionItem: { title: 'Become A Silver Member', icon: 'add' } as ActionItem
        } as Plan;
        fixture.detectChanges();
    });

    it('renders', () => {
        expect(component).toBeDefined();
    });

    describe('template', () => {
        beforeEach(() => {
            fixture.detectChanges();
        });

        it('shows the plan title icon', () => {
            validateExist(fixture, 'app-icon');
        });

        it('shows the plan title', () => {
            validateText(fixture, '.title', component.plan.title);
        });

        it('shows the plan copy/markup', () => {
            validateText(fixture, '.plan-copy div', 'SILVER TIER');
            validateText(fixture, '.plan-copy div', 'Get the good stuff without the extra fancy fluff.');
        });

        it('shows the plan signup action button', () => {
            validateText(fixture, '.actions button', component.plan.signupActionItem.title);
        });

        it('shows the plan signup action button icon', () => {
            validateExist(fixture, '.actions button app-icon');
        });
    });
});
