import { Component, Injector } from '@angular/core';
import { HomeInterface } from './home.interface';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { OpenposMediaService, MediaBreakpoints } from '../../core/media/openpos-media.service';
import { CONFIGURATION } from '../../configuration/configuration';
import { Observable, timer } from 'rxjs';
import { IActionItem } from '../../core/actions/action-item.interface';
import { INotificationItem } from '../../core/interfaces/notification-item.interface';
import { trigger, state, style, transition, useAnimation } from '@angular/animations';
import { map } from 'rxjs/operators';
import { bounceAnimation } from '../../shared/animations/bounce.animation';

@ScreenComponent({
  name: 'Home'
})
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  animations: [
    trigger('bounce', [
      state('move', style({
        transform: 'translateY(0)'
      })),
      transition('* => move', [
        useAnimation(bounceAnimation, {
          params: {
            height: '100px',
            time: '2s'
          }
        })
      ])
    ]),
  ]
})
export class HomeComponent extends PosScreenDirective<HomeInterface> {

  bounceInterval = timer(5000, 5000).pipe(map(i => i % 2 ? 'down' : 'move'));
  gutterSize: Observable<number>;
  gridColumns: Observable<number>;
  isMobile: Observable<boolean>;
  iconClass: Observable<string>;
  badgeSize: Observable<string>;

  constructor(media: OpenposMediaService, injector: Injector) {
    super(injector);
    this.gridColumns = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, 1],
      [MediaBreakpoints.MOBILE_LANDSCAPE, 2],
      [MediaBreakpoints.TABLET_PORTRAIT, 3],
      [MediaBreakpoints.TABLET_LANDSCAPE, 3],
      [MediaBreakpoints.DESKTOP_PORTRAIT, 3],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, 3]
    ]));

    this.gutterSize = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, 10],
      [MediaBreakpoints.MOBILE_LANDSCAPE, 10],
      [MediaBreakpoints.TABLET_PORTRAIT, 15],
      [MediaBreakpoints.TABLET_LANDSCAPE, 15],
      [MediaBreakpoints.DESKTOP_PORTRAIT, 20],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, 20]
    ]));

    this.isMobile = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, false],
      [MediaBreakpoints.TABLET_LANDSCAPE, false],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));

    this.iconClass = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, 'material-icons mat-24'],
      [MediaBreakpoints.MOBILE_LANDSCAPE, 'material-icons mat-24'],
      [MediaBreakpoints.TABLET_PORTRAIT, 'material-icons mat-24'],
      [MediaBreakpoints.TABLET_LANDSCAPE, 'material-icons mat-48'],
      [MediaBreakpoints.DESKTOP_PORTRAIT, 'material-icons mat-64'],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, 'material-icons mat-64']
    ]));

    this.badgeSize = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, 'medium'],
      [MediaBreakpoints.MOBILE_LANDSCAPE, 'medium'],
      [MediaBreakpoints.TABLET_PORTRAIT, 'medium'],
      [MediaBreakpoints.TABLET_LANDSCAPE, 'medium'],
      [MediaBreakpoints.DESKTOP_PORTRAIT, 'large'],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, 'large']
    ]));
  }

  buildScreen() { }

  public keybindsEnabled() {
    return CONFIGURATION.enableKeybinds;
  }

  public getNotificationForButton(item: IActionItem): INotificationItem {
    if (this.screen.notificationItems) {
      return this.screen.notificationItems.find(i => i.id === item.action);
    }
    return null;
  }

  // '#f9a842'
  // colors = ['#1C9DD5', '#0657A1', '#282560', '#00A69B', '#37AC48', '#E83253', '#F9D63B', '#f9a842'];
  // colors = ['#E7506E', '#4FAC5A','#F8CE0D','#1D9ED1']; // light
  colors = ['#282560', '#0657A1','#1C9DD5', '#37AC48', '#FAD225', '#f9a842', '#E83253'];

  public getButtonColor(index: number) : string {
    return "#404040";
//     return this.colors[index % this.colors.length];
  }
}
