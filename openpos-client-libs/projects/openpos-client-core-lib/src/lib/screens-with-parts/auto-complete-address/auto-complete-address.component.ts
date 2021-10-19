import { Component } from '@angular/core';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';

@ScreenComponent({
    name: 'AutoCompleteAddress'
})
@Component({
  selector: 'app-auto-complete-address',
  templateUrl: './auto-complete-address.component.html',
  styleUrls: ['./auto-complete-address.component.scss']
})
export class AutoCompleteAddressComponent extends PosScreenDirective<any> {

  buildScreen() { }

}
