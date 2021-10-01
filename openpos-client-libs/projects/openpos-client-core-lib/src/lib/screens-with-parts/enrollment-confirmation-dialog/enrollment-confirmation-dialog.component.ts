import { PosScreen } from '../pos-screen/pos-screen.component';
import {Component, Injector, OnInit} from '@angular/core';
import {
  EnrollmentConfirmationDialogInterface
} from './enrollment-confirmation-dialog.interface';
import { DialogComponent } from '../../shared/decorators/dialog-component.decorator';
import {SafeHtml} from '@angular/platform-browser/src/security/dom_sanitization_service';
import {DomSanitizer} from '@angular/platform-browser';
import {IActionItem} from '../../core/actions/action-item.interface';
import {Configuration} from '../../configuration/configuration';

@DialogComponent({
    name: 'EnrollmentConfirmationDialog'
})
@Component({
  selector: 'app-enrollment-confirmation-dialog',
  templateUrl: './enrollment-confirmation-dialog.html',
  styleUrls: [ './enrollment-confirmation-dialog.component.scss']
})
export class EnrollmentConfirmationDialogComponent extends PosScreen<EnrollmentConfirmationDialogInterface> implements OnInit {

  safeProgramCopy: SafeHtml;

  constructor(injector: Injector, private sanitizer: DomSanitizer) {
    super(injector);
  }

  ngOnInit(): void {
    this.safeProgramCopy = this.sanitizer.bypassSecurityTrustHtml(this.screen.programCopy);
  }

  buildScreen() {
  }

  public keybindsEnabled(menuItem: IActionItem): boolean {
    return Configuration.enableKeybinds && !!menuItem.keybind;
  }
}
