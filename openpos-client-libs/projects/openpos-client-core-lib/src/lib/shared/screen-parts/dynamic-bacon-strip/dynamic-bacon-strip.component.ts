import { Component, Injector } from '@angular/core';
import { DynamicBaconStripInterface } from './dynamic-bacon-strip.interface';
import { ScreenPart } from '../../decorators/screen-part.decorator';
import { ScreenPartComponent } from '../screen-part';
import { Observable } from 'rxjs';
import { OpenposMediaService, MediaBreakpoints } from '../../../core/media/openpos-media.service';
import {BaconStripInterface} from "../bacon-strip/bacon-strip.interface";


@ScreenPart({
  name: 'dynamicBaconStrip'
})
@Component({
  selector: 'app-dynamic-bacon-strip',
  templateUrl: './dynamic-bacon-strip.component.html',
  styleUrls: ['./dynamic-bacon-strip.component.scss']
})
export class DynamicBaconStripComponent extends ScreenPartComponent<DynamicBaconStripInterface> {

  isMobile: Observable<boolean>;

  constructor(injector: Injector, private media: OpenposMediaService) {
    super(injector);

    this.isMobile = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, true],
      [MediaBreakpoints.TABLET_LANDSCAPE, false],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));
  }
  screenDataUpdated() { }

  handleClick() {
    if(this.screenData.actions && this.screenData.actions.length === 1) {
      this.doAction(this.screenData.actions[0]);
    }
  }
}
