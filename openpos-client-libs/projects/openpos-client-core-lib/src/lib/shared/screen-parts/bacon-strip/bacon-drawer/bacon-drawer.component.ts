import { Component, EventEmitter, Injector, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { IActionItem } from '../../../../core/actions/action-item.interface';
import { ScreenPart } from '../../../decorators/screen-part.decorator';
import { ScreenPartComponent } from '../../screen-part';
import { BaconStripInterface } from '../bacon-strip.interface';
import { KeyPressProvider } from '../../../providers/keypress.provider';
import { CONFIGURATION } from '../../../../configuration/configuration';
import { merge } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { KeybindingZoneService } from '../../../../core/keybindings/keybinding-zone.service';

@ScreenPart({ name: 'baconStrip' })
@Component({
  selector: 'app-bacon-drawer',
  templateUrl: './bacon-drawer.component.html',
  styleUrls: ['./bacon-drawer.component.scss'],
  providers: [KeybindingZoneService]
})
export class BaconDrawerComponent extends ScreenPartComponent<BaconStripInterface> implements OnInit, OnDestroy {
  @Output()
  buttonClicked = new EventEmitter();
  keyPressProvider: KeyPressProvider;
  stop$ = merge(this.beforeScreenDataUpdated$, this.destroyed$);
  keybindsEnabled: boolean;
  keybindingZoneService: KeybindingZoneService;

  constructor(injector: Injector) {
    super(injector);
    this.keybindingZoneService = injector.get(KeybindingZoneService);
    this.keybindingZoneService.register('bacon-strip-drawer');
  }

  screenDataUpdated() {
    this.keybindingZoneService.updateZone(this.screenData);
    this.keybindsEnabled = CONFIGURATION.enableKeybinds;

    if (this.keybindsEnabled) {
      this.subscribeToActionKeybinds(this.screenData.actions);
      this.subscribeToActionKeybinds(this.screenData.operatorMenu);
    }
  }

  private subscribeToActionKeybinds(actions) {
    // Give these keys low priority so that keybindings inside the screen can take priority
    if (actions) {
      this.keyPressProvider.subscribe(actions, 90, (event, action) => this.doAction(action), this.stop$);

      this.keyPressProvider.globalSubscribe(actions).pipe(
          takeUntil(this.stop$)
      ).subscribe(action => super.doAction(action));
    }
  }

  buttonClick(action: IActionItem) {
    this.buttonClicked.emit();
    super.doAction(action);
  }

  ngOnDestroy() {
    this.keybindingZoneService.unregister();
    super.ngOnDestroy();
  }
}
