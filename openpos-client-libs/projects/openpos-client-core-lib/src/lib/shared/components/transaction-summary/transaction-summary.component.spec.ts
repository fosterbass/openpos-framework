import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransactionSummaryComponent } from './transaction-summary.component';
import { ActionService } from '../../../core/actions/action.service';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { OpenposMediaService } from '../../../core/media/openpos-media.service';
import { ElectronService } from 'ngx-electron';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { Observable, of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import {IconComponent} from '../icon/icon.component';
import { MockComponent } from 'ng-mocks';
import {IconButtonComponent} from '../icon-button/icon-button.component';
import {CurrencyTextComponent} from '../currency-text/currency-text.component';

class MockActionService { }
class MockMatDialog { }
class MockElectronService { }
class ClientContext { }

describe('TransactionSummaryComponent', () => {
  let component: TransactionSummaryComponent;
  let fixture: ComponentFixture<TransactionSummaryComponent>;

  class MockOpenposMediaServiceMobileFalse {
    observe(): Observable<boolean> {
      return of(false);
    }
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MatChipsModule, HttpClientTestingModule],
      declarations: [
        TransactionSummaryComponent,
        MockComponent(IconComponent),
        MockComponent(IconButtonComponent),
        MockComponent(CurrencyTextComponent)
      ],
      providers: [
        { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
        { provide: ActionService, useCass: MockActionService },
        { provide: MatDialog, useClass: MockMatDialog },
        { provide: ElectronService, useClass: MockElectronService },
        { provide: ClientContext, useValue: {} },
        { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TransactionSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
