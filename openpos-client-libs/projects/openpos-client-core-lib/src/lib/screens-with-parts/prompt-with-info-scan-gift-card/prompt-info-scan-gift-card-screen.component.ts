import { Component } from '@angular/core';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { PromptWithInfoScanGiftCardInterface} from './prompt-with-info-scan-gift-card.interface';

/**
 * @ignore
 */
@ScreenComponent({
  name: 'PromptWithInfoScanGiftCard'
})
@Component({
  selector: 'app-prompt-with-info-scan-gift-card-screen',
  templateUrl: './prompt-with-info-scan-gift-card-screen.component.html',
  styleUrls: ['./prompt-with-info-scan-gift-card-screen.component.scss']
})
export class PromptInfoScanGiftCardScreenComponent extends PosScreenDirective<PromptWithInfoScanGiftCardInterface> {

  buildScreen() {
  }
}
