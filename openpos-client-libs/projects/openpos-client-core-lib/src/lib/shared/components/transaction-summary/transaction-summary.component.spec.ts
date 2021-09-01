import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { TransactionSummaryComponent } from './transaction-summary.component';
import { Component, Input } from '@angular/core';
import { ActionService } from '../../../core/actions/action.service';
import { MatChipsModule } from '@angular/material';
import {OpenposMediaService} from "../../../core/media/openpos-media.service";
import {Observable, of} from "rxjs";

describe('TransactionSummaryComponent', () => {
  let component: TransactionSummaryComponent;
  let fixture: ComponentFixture<TransactionSummaryComponent>;

  class MockOpenposMediaServiceMobileFalse {
    observe(): Observable<boolean> {
      return of(false);
    }
  };

  beforeEach(async(() => {

    TestBed.configureTestingModule({
      imports: [MatChipsModule],
      declarations: [
        TransactionSummaryComponent,
        MockIconComponent,
        MockIconButtonComponent,
        MockCurrencyTextComponent,
      ],
      providers: [
        { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
        { provide: ActionService }
      ]
    })
      .compileComponents();
  }));

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
  selector: 'app-icon-button',
  template: '',
})
class MockIconButtonComponent {
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
