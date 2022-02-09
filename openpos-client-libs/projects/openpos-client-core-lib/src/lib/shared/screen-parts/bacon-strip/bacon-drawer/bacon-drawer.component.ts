import { Component, EventEmitter, Injector, OnDestroy, OnInit, Output } from '@angular/core';
import { IActionItem } from '../../../../core/actions/action-item.interface';
import { ScreenPart } from '../../../decorators/screen-part.decorator';
import { ScreenPartComponent } from '../../screen-part';
import { BaconStripInterface } from '../bacon-strip.interface';
import { CONFIGURATION } from '../../../../configuration/configuration';
import { KeybindingZoneService } from '../../../../core/keybindings/keybinding-zone.service';
import { filter, takeUntil } from 'rxjs/operators';

@ScreenPart({ name: 'baconStrip' })
@Component({
  selector: 'app-bacon-drawer',
  templateUrl: './bacon-drawer.component.html',
  styleUrls: ['./bacon-drawer.component.scss'],
  providers: [KeybindingZoneService]
})
export class BaconDrawerComponent extends ScreenPartComponent<BaconStripInterface> implements OnInit, OnDestroy {
  @Output()
  buttonClicked = new EventEmitter<IActionItem>();
  keybindsEnabled: boolean;
  keybindingZoneService: KeybindingZoneService;

  constructor(injector: Injector) {
    super(injector);
    this.keybindingZoneService = injector.get(KeybindingZoneService);

    this.keybindingZoneService.register({
      id: 'bacon-strip-drawer',
      autoDoAction: false
    }).pipe(
        takeUntil(this.destroyed$)
    ).subscribe(event => this.buttonClick(event.action));
  }

  screenDataUpdated() {
    this.keybindingZoneService.updateZone(this.screenData);
    this.keybindsEnabled = CONFIGURATION.enableKeybinds;
  }

  buttonClick(action: IActionItem) {
    this.buttonClicked.emit(action);
  }

  ngOnDestroy() {
    this.keybindingZoneService.unregister();
    super.ngOnDestroy();
  }
}
