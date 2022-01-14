import { Component, Input, HostListener, ViewChild, OnDestroy } from '@angular/core';
import { ISellItem } from '../../../core/interfaces/sell-item.interface';
import { SessionService } from '../../../core/services/session.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { ActionService } from '../../../core/actions/action.service';
import {Observable, Subject, Subscription } from 'rxjs';
import { CONFIGURATION } from '../../../configuration/configuration';
import { KeyPressProvider } from '../../providers/keypress.provider';
import { KeyboardClassKey } from '../../../keyboard/enums/keyboard-class-key.enum';
import { KebabLabelButtonComponent } from '../kebab-label-button/kebab-label-button.component';
import {MediaBreakpoints, OpenposMediaService} from '../../../core/media/openpos-media.service';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { filter, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-item-card',
  templateUrl: './item-card.component.html',
  styleUrls: ['./item-card.component.scss']
})
export class ItemCardComponent implements OnDestroy {
  private destroyed$ = new Subject();
  private _item: ISellItem;

  @Input() set item(item: ISellItem) {
    this._item = item;
  }

  get item() {
    return this._item;
  }

  @Input() isReadOnly = false;

  _expanded = true;
  @Input('expanded')
  set expanded(expanded: boolean) {
    this._expanded = expanded;
    if (!this._expanded && this.buttonSubscription) {
      this.buttonSubscription.unsubscribe();
    } else if (this._expanded) {
      this.createSubscription();
    }
  }
  get expanded() {
    return this._expanded;
  }

  @Input() enableHover = true;

  public hover = false;

  @ViewChild('kebab') kebab: KebabLabelButtonComponent;
  buttonSubscription: Subscription;

  isMobile$: Observable<boolean>;

  constructor(public actionService: ActionService, public session: SessionService,
              protected keyPresses: KeyPressProvider, private mediaService: OpenposMediaService,
              private keybindingZoneService: KeybindingZoneService) {
    this.createSubscription();
    this.isMobile$ = mediaService.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, true],
      [MediaBreakpoints.TABLET_LANDSCAPE, true],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));
  }

  public createSubscription() {
    this.buttonSubscription = this.keyPresses.subscribe(KeyboardClassKey.Space, 1, (event: KeyboardEvent) => {
      // ignore repeats and check configuration
      if (event.repeat || event.type !== 'keydown' || !CONFIGURATION.enableKeybinds) {
        return;
      }
      if (event.type === 'keydown' && this.expanded) {
        this.handleKeyDown();
      }
    });

    this.keybindingZoneService.getKeyDownEvent(' ')
        .pipe(
            filter(() => this.expanded),
            filter(event => !event.domEvent.repeat),
            takeUntil(this.destroyed$)
        ).subscribe(() => this.handleKeyDown());
  }

  handleKeyDown(): void {
    if (this.item.menuItems.length > 1) {
      this.kebab.openKebabMenu();
    } else {
      this.doItemAction(this.item.menuItems[0], this.item.index);
    }
  }

  ngOnDestroy() {
    if (this.buttonSubscription) {
      this.buttonSubscription.unsubscribe();
    }
    if (this.kebab) {
      this.kebab.closeKebabMenu();
    }
    this.destroyed$.next();
  }

  public doItemAction(action: IActionItem, payload: number) {
    this.actionService.doAction(action, [payload]);
  }

  public isMenuItemEnabled(m: IActionItem): boolean {
    let enabled = m.enabled;
    if (m.action.startsWith('<') && this.session.isRunningInBrowser()) {
      enabled = false;
    }
    return enabled;
  }

  @HostListener('mouseenter')
  onMouseEnter() {
    if (this.enableHover) {
      this.hover = true;
    }
  }

  @HostListener('mouseleave')
  onMouseLeave() {
    this.hover = false;
  }

}
