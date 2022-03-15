import { Component, EventEmitter, Injector, Input , Output} from '@angular/core';
import { DynamicBaconStripInterface } from './dynamic-bacon-strip.interface';
import { ScreenPart } from '../../decorators/screen-part.decorator';
import { ScreenPartComponent } from '../screen-part';
import { Observable } from 'rxjs';
import { OpenposMediaService, MediaBreakpoints } from '../../../core/media/openpos-media.service';


@ScreenPart({
  name: 'dynamicBaconStrip'
})
@Component({
  selector: 'app-dynamic-bacon-strip',
  templateUrl: './dynamic-bacon-strip.component.html',
  styleUrls: ['./dynamic-bacon-strip.component.scss']
})
export class DynamicBaconStripComponent extends ScreenPartComponent<DynamicBaconStripInterface> {

  @Input()
  hideLogo: boolean;

  /**
   * A flag indicating whether the behavior of the left ActionItem button/icon on the baconstrip should be ignored
   * when the button/icon is clicked.
   *
   * If this ever evaluates to true, you should also have an actionOverride specified.
   * @see actionOverride
   */
  @Input()
  actionOverrideFlag = false;

  /**
   * The function that is called when this baconstrip component's ActionItem button/icon is clicked if the
   * actionOverrideFlag is true.
   * <hr/>
   * <b>Warning:</b> The baconstrip action provided by the server state is typically a “Back” action or something else
   * to be handled by the state. If you are not careful, overriding this baconstrip action may cause the app to get
   * stuck on this screen without some other way of triggering an action to be handled by the server state.
   * <hr/>
   * @see actionOverrideFlag
   */
  @Output()
  actionOverride: EventEmitter<any> = new EventEmitter<any>();

  isMobile: Observable<boolean>;

  constructor(injector: Injector, private media: OpenposMediaService) {
    super(injector);

    this.isMobile = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, false],
      [MediaBreakpoints.TABLET_LANDSCAPE, false],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));
  }
  screenDataUpdated() { }

  handleClick() {
    if (this.actionOverrideFlag && this.actionOverride) {
      this.actionOverride.emit();
    } else if (this.screenData.actions && this.screenData.actions.length >= 1) {
      this.doAction(this.screenData.actions[0]);
    }
  }
}
