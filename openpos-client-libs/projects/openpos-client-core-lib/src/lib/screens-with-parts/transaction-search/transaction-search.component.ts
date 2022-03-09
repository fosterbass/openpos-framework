import { Component, Injector } from '@angular/core';
import { TransactionSearchInterface } from './transaction-search.interface';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { TransactionSearchMode } from './transaction-search-mode.enum';
import { IDynamicFormPartEventArg } from '../../shared/screen-parts/dynamic-form-part/dynamic-form-part-event-arg.interface';
import { IForm } from '../../core/interfaces/form.interface';
import { UIDataMessageService } from '../../core/ui-data-message/ui-data-message.service';
import { takeUntil } from 'rxjs/operators';
import { merge, Observable } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../core/media/openpos-media.service';
import { IActionItem } from '../../core/actions/action-item.interface';

@ScreenComponent({
  name: 'TransactionSearch'
})
@Component({
  selector: 'app-transaction-search',
  templateUrl: './transaction-search.component.html',
  styleUrls: ['./transaction-search.component.scss']
})
export class TransactionSearchComponent extends PosScreenDirective<TransactionSearchInterface> {
  searchAllParamsForm: IForm;
  resultsCount: number;
  TransactionSearchMode = TransactionSearchMode;
  changeSearchModeDisabled: boolean;

  isMobile: Observable<boolean>;

  constructor(injector: Injector, protected dataMessageService: UIDataMessageService, media: OpenposMediaService) {
    super(injector);
    this.isMobile = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, true],
      [MediaBreakpoints.TABLET_LANDSCAPE, false],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));
  }

  buildScreen() {
    this.dataMessageService.getData$(this.screen.providerKey)
        .pipe(
            takeUntil(merge(this.destroyed$, this.beforeBuildScreen$))
        )
        .subscribe(results => this.resultsCount = results.length);
    this.changeSearchModeDisabled = !this.screen.changeSearchModeButton;
  }

  onSearchAllFormChanges(event: IDynamicFormPartEventArg): void {
    this.searchAllParamsForm = event.form;
  }

  searchAll(): void {
    this.doAction(this.screen.searchAllButton.action, this.searchAllParamsForm);
  }

  filterChanged(filter: IActionItem): void {
    this.doAction(filter.action);
  }
}
