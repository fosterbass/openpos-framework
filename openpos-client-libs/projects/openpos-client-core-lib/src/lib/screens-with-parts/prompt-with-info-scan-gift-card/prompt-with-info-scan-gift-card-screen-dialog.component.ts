import { PromptInfoScanGiftCardScreenComponent} from './prompt-info-scan-gift-card-screen.component';
import { DialogComponent } from '../../shared/decorators/dialog-component.decorator';
import { Component } from '@angular/core';

@DialogComponent({
    name: 'PromptWithInfoScanGiftCard'
})
@Component({
    selector: 'app-prompt-with-info-scan-gift-card-screen-dialog',
    templateUrl: './prompt-with-info-scan-gift-card-screen-dialog.component.html',
    styleUrls: ['./prompt-with-info-scan-gift-card-screen-dialog.component.scss']
})
export class PromptWithInfoScanGiftCardScreenDialogComponent extends PromptInfoScanGiftCardScreenComponent {
}
