import { ScreenPart } from '../../decorators/screen-part.decorator';
import {Component, Injector, Input} from '@angular/core';
import { ScreenPartComponent } from '../screen-part';
import {DialogHeaderInterface} from '../dialog-header/dialog-header.interface';

@ScreenPart({
    name: 'dualActionDialogHeader'})
@Component({
    selector: 'app-dual-action-dialog-header',
    templateUrl: './dual-action-dialog-header.component.html',
    styleUrls: ['./dual-action-dialog-header.component.scss']
})
export class DualActionDialogHeaderComponent extends ScreenPartComponent<DialogHeaderInterface> {

    @Input()
    headerIcon: string;

    @Input()
    headerIconClass: string;

    @Input()
    headerText: string;

    constructor( injector: Injector) {
        super(injector);
    }

    screenDataUpdated() {
        this.screenData.headerText = this.headerText ? this.headerText : this.screenData.headerText;
        this.screenData.headerIcon = this.headerIcon ? this.headerIcon : this.screenData.headerIcon;
        if (this.screenData.backButton) {
            this.screenData.backButton.keybind = 'Escape';
        }
    }
}
