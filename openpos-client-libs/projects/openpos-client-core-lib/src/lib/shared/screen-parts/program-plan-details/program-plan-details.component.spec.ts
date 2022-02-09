import { ActionService } from '../../../core/actions/action.service';
import { validateExist} from '../../../utilites/test-utils';
import { MatDialog } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ElectronService } from 'ngx-electron';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { Subscription } from 'rxjs';
import { ProgramPlanDetailsComponent } from './program-plan-details.component';
import { Plan } from '../../../screens-with-parts/membership/plan-interface';
import { ActionItem } from '../../../core/actions/action-item';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { SafeHtmlPipe } from '../../pipes/safe-html.pipe';
import { MockComponent } from 'ng-mocks';
import { PlanDetailsDisplayComponent } from '../plan-details-display/plan-details-display.component';

class MockActionService { }
class MockMatDialog { }
class MockElectronService { }
class ClientContext { }

describe('ProgramPlanDetailsComponent', () => {
    let component: ProgramPlanDetailsComponent;
    let fixture: ComponentFixture<ProgramPlanDetailsComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            declarations: [
                ProgramPlanDetailsComponent,
                MockComponent(PlanDetailsDisplayComponent),
                SafeHtmlPipe
            ],
            providers: [
                { provide: MatDialog, useClass: MockMatDialog },
                { provide: ActionService, useClass: MockActionService },
                { provide: ElectronService, useClass: MockElectronService },
                { provide: ClientContext, useValue: {} },
                { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
            ]
        }).compileComponents();
        fixture = TestBed.createComponent(ProgramPlanDetailsComponent);
        component = fixture.componentInstance;
        component.programCopy = '<div styles=\'color: green\'>Hello World</div>' as string;
        component.plans = [{
            iconImageUrl: 'iconImageUrl',
            iconText: 'iconText',
            title: 'title',
            copy: 'copy',
            signupActionItem: {} as ActionItem
        } as Plan,
        {
            iconImageUrl: 'iconImageUrl2',
            iconText: 'iconText2',
            title: 'title2',
            copy: 'copy2',
            signupActionItem: {} as ActionItem
        } as Plan];

        fixture.detectChanges();
    });

    it('renders', () => {
        expect(component).toBeDefined();
    });

    describe('template', () => {
        it('should display any safeProgramCopy given', () => {
            component.programCopy = 'dumb';
            fixture.detectChanges();
            validateExist(fixture, '.program div');
        });

        it('should display not display programCopy when there is no copy', () => {
            component.programCopy = 'dumb';
            fixture.detectChanges();
            validateExist(fixture, '.program div');
        });

        it('should display any plans given', () => {
            const selector = '.plans app-plan-details-display';
            validateExist(fixture, selector);
            const element = fixture.debugElement.queryAll(By.css(selector));
            expect(element.length).toEqual(component.plans.length);
        });
    });
});
