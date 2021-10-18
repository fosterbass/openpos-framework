import { Component } from '@angular/core';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { PromptWithInfoInterface } from './prompt-with-info.interface';

/**
 * @ignore
 */
@ScreenComponent({
  name: 'PromptWithInfo'
})
@Component({
  selector: 'app-prompt-with-info-screen',
  templateUrl: './prompt-with-info-screen.component.html',
  styleUrls: ['./prompt-with-info-screen.component.scss']
})
export class PromptWithInfoScreenComponent extends PosScreenDirective<PromptWithInfoInterface> {

  buildScreen() {
  }
}
