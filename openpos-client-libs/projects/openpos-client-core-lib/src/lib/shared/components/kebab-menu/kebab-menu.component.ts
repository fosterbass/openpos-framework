import { KeyPressProvider } from './../../providers/keypress.provider';
import { Component, Inject, OnDestroy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject, Subscription } from 'rxjs';
import { CONFIGURATION } from '../../../configuration/configuration';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-kebab-menu',
    templateUrl: './kebab-menu.component.html',
    styleUrls: ['./kebab-menu.component.scss'],
    providers: [KeybindingZoneService]
})
export class KebabMenuComponent implements OnDestroy {
    private destroyed$ = new Subject();
    protected subscriptions: Subscription = new Subscription();

    constructor(@Inject(MAT_DIALOG_DATA) public data: any,
                public dialogRef: MatDialogRef<KebabMenuComponent>,
                protected keyPresses: KeyPressProvider,
                protected keybindingZoneService: KeybindingZoneService) {

        this.registerKeybindings();

        if (CONFIGURATION.enableKeybinds) {
            this.data.menuItems.forEach(item => {
                if (!!item.keybind) {
                    this.subscriptions.add(
                        this.keyPresses.subscribe(item.keybind, 100, event => {
                            // ignore repeats
                            if (event.repeat || !CONFIGURATION.enableKeybinds) {
                                return;
                            }
                            if (event.type === 'keydown') {
                                this.closeMenu(item);
                            }
                        })
                    );
                }
            });
        }
    }

    registerKeybindings(): void {
        this.keybindingZoneService.register({
            id: 'kebab-menu',
            actionsObj: this.data,
            // Only set for clarity - actions won't automatically execute anyway because there's no available action service here
            autoDoAction: false
        }).pipe(
            takeUntil(this.destroyed$)
        ).subscribe(event => this.closeMenu(event.action));

        this.keybindingZoneService.activate();
    }

    ngOnDestroy(): void {
        if (this.subscriptions) {
            this.subscriptions.unsubscribe();
        }
        this.destroyed$.next();
        this.keybindingZoneService.unregister();
        this.keybindingZoneService.restorePreviousActivation();
    }

    closeMenu(option: any) {
        this.dialogRef.close(option);
        this.keybindingZoneService.deactivate();
    }

}
