import { PosScreenDirective } from '../../pos-screen/pos-screen.component';
import { Component, Injector } from '@angular/core';
import {
  EnrollmentConfirmationDialogInterface
} from './enrollment-confirmation-dialog.interface';
import { DialogComponent } from '../../../shared/decorators/dialog-component.decorator';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { CONFIGURATION } from '../../../configuration/configuration';

@DialogComponent({
  name: 'EnrollmentConfirmationDialog'
})
@Component({
  selector: 'app-enrollment-confirmation-dialog',
  templateUrl: './enrollment-confirmation-dialog.html',
  styleUrls: ['./enrollment-confirmation-dialog.component.scss']
})
export class EnrollmentConfirmationDialogComponent extends PosScreenDirective<EnrollmentConfirmationDialogInterface> {


  constructor(injector: Injector) {
    super(injector);
  }

  buildScreen() {
  }

  public keybindsEnabled(menuItem: IActionItem): boolean {
    return CONFIGURATION.enableKeybinds && !!menuItem.keybind;
  }
}
