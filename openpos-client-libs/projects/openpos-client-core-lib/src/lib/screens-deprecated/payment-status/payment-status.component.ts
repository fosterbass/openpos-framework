import {Component } from '@angular/core';
import { PosScreen } from '../pos-screen/pos-screen.component';
import { IActionItem } from '../../core/interfaces/action-item.interface';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';

/**
 * @ignore
 */
@ScreenComponent({
  name: 'PaymentStatus'
})
@Component({
  selector: 'app-payment-status',
  templateUrl: './payment-status.component.html'
})
export class PaymentStatusComponent extends PosScreen<any> {

  screen: any;
  pinPadStatus = '0';
  bypassEnabled = false;
  
  buildScreen() {
    this.pinPadStatus = this.screen.pinPadStatus;
    setTimeout(()=>{                          
      this.bypassEnabled = true;
    }, this.screen.delay);
  }

  public doMenuItemAction(menuItem: IActionItem) {
    this.doAction(menuItem);
  }

}
