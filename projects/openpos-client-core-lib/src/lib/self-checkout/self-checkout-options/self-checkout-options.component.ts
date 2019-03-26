import { Component, OnInit, OnDestroy } from '@angular/core';
import { PosScreen } from '../../screens-deprecated/pos-screen/pos-screen.component';
import { IOptionItem } from '../../screens-deprecated/choose-options/option-item.interface';
import { SessionService } from '../../core/services/session.service';
import { ActionIntercepter, ActionIntercepterBehaviorType } from '../../core/action-intercepter';


@Component({
  selector: 'app-self-checkout-options',
  templateUrl: './self-checkout-options.component.html',
  styleUrls: ['./self-checkout-options.component.scss']
})
export class SelfCheckoutOptionsComponent extends PosScreen<any> implements  OnInit, OnDestroy {

  static readonly UNDO = 'Undo';
  public currentView: string;
  public selectedOption: IOptionItem;
  public optionItems: IOptionItem[];

  constructor(public session: SessionService) {
      super();
  }

  buildScreen() {
    this.optionItems = this.screen.options;
    this.currentView = this.screen.displayStyle;
  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.session.unregisterActionIntercepter(SelfCheckoutOptionsComponent.UNDO);
  }

  onMakeOptionSelection(option: IOptionItem): void {
    if (option.form.formElements.length > 0) {
      this.selectedOption = option;
      this.currentView = 'OptionForm';
      this.session.registerActionIntercepter(SelfCheckoutOptionsComponent.UNDO,
        new ActionIntercepter(this.log, (payload) => { this.onBackButtonPressed(); }, ActionIntercepterBehaviorType.block));
    } else {
      this.session.onAction(option.value);
    }
  }

  onBackButtonPressed(): void {
    this.currentView = this.screen.displayStyle;
    this.session.unregisterActionIntercepter(SelfCheckoutOptionsComponent.UNDO);
  }

}
