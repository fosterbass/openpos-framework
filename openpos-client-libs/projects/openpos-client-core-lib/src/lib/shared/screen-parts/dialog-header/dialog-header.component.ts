import { ScreenPart } from '../../decorators/screen-part.decorator';
import { Component, Injector, Input } from '@angular/core';
import { DialogHeaderInterface } from './dialog-header.interface';
import { ScreenPartComponent } from '../screen-part';

@ScreenPart({
    name: 'dialogHeader'})
@Component({
    selector: 'app-dialog-header',
    templateUrl: './dialog-header.component.html',
    styleUrls: ['./dialog-header.component.scss']
})
export class DialogHeaderComponent extends ScreenPartComponent<DialogHeaderInterface> {

    @Input()
    headerIcon: string;

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
