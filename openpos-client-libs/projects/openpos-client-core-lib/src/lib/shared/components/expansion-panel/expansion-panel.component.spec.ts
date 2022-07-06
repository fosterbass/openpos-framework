import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MockComponent } from 'ng-mocks';
import { IconComponent } from '../icon/icon.component';
import { ExpansionPanelComponent } from './expansion-panel.component';

describe('ExpansionPanelComponent', () => {
    let expansionPanel: ExpansionPanelComponent;
    let fixture: ComponentFixture<ExpansionPanelComponent>;
    const expansionHeaderSelector = '.expansion-header';
    const expandedIconId = '#expandedIcon';
    const closedIconId = '#closedIcon';
    let emittedValue: boolean;

    const isShowing = (selector: string): void => {
        expect(fixture.nativeElement.querySelector(selector)).not.toBeNull();
    };
    const isHidden = (selector: string): void => {
        expect(fixture.nativeElement.querySelector(selector)).toBeNull();
    };
    const isExpanded = (): void => {
        expect(expansionPanel.state).toBe('open');
        isShowing(expandedIconId);
        isHidden(closedIconId);
    };
    const isClosed = (): void => {
        expect(expansionPanel.state).toBe('close');
        isHidden(expandedIconId);
        isShowing(closedIconId);
    };
    const clickHeader = (): void => {
        fixture.nativeElement.querySelector(expansionHeaderSelector).click();
        fixture.detectChanges();
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [NoopAnimationsModule],
            declarations: [ExpansionPanelComponent, MockComponent(IconComponent)]
        }).compileComponents();
        fixture = TestBed.createComponent(ExpansionPanelComponent);
        expansionPanel = fixture.componentInstance;
        fixture.detectChanges();
        expansionPanel.expansionToggled.subscribe((expanded: boolean) => {
            emittedValue = expanded;
        });
    });

    it('renders', () => {
        expect(expansionPanel).toBeDefined();
    });
    it('defaults to starting as open', () => {
        isExpanded();
    });
    it('closes if you pass in false for expanded', () => {
        isExpanded();
        expansionPanel.expanded = false;
        expansionPanel.ngOnChanges();
        fixture.detectChanges();
        isClosed();
    });
    it('closes if you click the header', () => {
        isExpanded();
        clickHeader();
        isClosed();
    });
    it('opens if you click the header after passing in false for expanded', () => {
        expansionPanel.expanded = false;
        expansionPanel.ngOnChanges();
        fixture.detectChanges();
        isClosed();
        clickHeader();
        isExpanded();
    });
    it('emits a boolean denoting whether the panel is expanded when the state is changed', (complete: DoneFn) => {
        clickHeader();
        setTimeout(() => {
            expect(emittedValue).toBeFalse();
            complete();
        });
    });
});
