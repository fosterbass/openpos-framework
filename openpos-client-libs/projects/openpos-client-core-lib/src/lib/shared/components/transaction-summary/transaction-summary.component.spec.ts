import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransactionSummaryComponent } from './transaction-summary.component';
import { Component, Input } from '@angular/core';
import { ActionService } from '../../../core/actions/action.service';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { OpenposMediaService } from '../../../core/media/openpos-media.service';
import { ElectronService } from 'ngx-electron';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { HttpClientTestingModule } from '@angular/common/http/testing';

class MockActionService { }
class MockMatDialog { }
class MockElectronService { }
class ClientContext { }
class MockOpenposMediaService { }

describe('TransactionSummaryComponent', () => {
  let component: TransactionSummaryComponent;
  let fixture: ComponentFixture<TransactionSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatChipsModule, HttpClientTestingModule],
      declarations: [
        TransactionSummaryComponent,
        MockIconComponent,
        MockCurrencyTextComponent,
      ],
      providers: [
        { provide: ActionService, useCass: MockActionService },

        { provide: MatDialog, useClass: MockMatDialog },
        { provide: OpenposMediaService, useClass: MockOpenposMediaService },
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

@Component({
  selector: 'app-icon',
  template: '',
})
class MockIconComponent {
  @Input() iconName: string;
  @Input() iconClass: string;
}

@Component({
  selector: 'app-currency-text',
  template: '',
})
class MockCurrencyTextComponent {
  @Input() amountText: string;
}
