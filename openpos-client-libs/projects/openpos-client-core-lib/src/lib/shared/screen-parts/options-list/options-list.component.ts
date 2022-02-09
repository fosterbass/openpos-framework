import { Component, EventEmitter, Injector, Input, OnDestroy, Output } from '@angular/core';
import { OptionsListInterface } from './options-list.interface';
import { ScreenPart } from '../../decorators/screen-part.decorator';
import { ScreenPartComponent } from '../screen-part';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { merge, Observable } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { FocusService } from '../../../core/focus/focus.service';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { KebabMenuComponent } from '../../components/kebab-menu/kebab-menu.component';
import { takeUntil } from 'rxjs/operators';
import { CONFIGURATION } from '../../../configuration/configuration';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';

@ScreenPart({
    name: 'optionsList'
})
@Component({
    selector: 'app-options-list',
    templateUrl: './options-list.component.html',
    styleUrls: ['./options-list.component.scss']
})
export class OptionsListComponent extends ScreenPartComponent<OptionsListInterface> implements OnDestroy {

    @Output()
    optionClick = new EventEmitter<IActionItem>();

    @Input()
    listSize = -1;

    @Input()
    optionListSizeClass = 'lg';

    @Input()
    overflowPanelClass = '';

    @Input()
    overflowPanelWidth = '';

    options: IActionItem[] = [];
    overflowOptions: IActionItem[] = [];
    dialogRef: MatDialogRef<any>;
    isMobile: Observable<boolean>;

    autoFocusFirstOption = CONFIGURATION.autoFocusFirstOptionsListOption;

    constructor(
        injector: Injector,
        mediaService: OpenposMediaService,
        protected dialog: MatDialog,
        protected focusService: FocusService,
        protected keybindingZoneService: KeybindingZoneService
    ) {

        super(injector);
        this.isMobile = mediaService.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, true],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));
    }

    screenDataUpdated() {
        if (this.listSize > 0 && this.screenData.options && this.listSize < this.screenData.options.length) {
            this.options = [];
            this.overflowOptions = [];
            for (let i = 0; i < this.screenData.options.length; i++) {
                if (i < this.listSize) {
                    this.options.push(this.screenData.options[i]);
                } else {
                    this.overflowOptions.push(this.screenData.options[i]);
                }
            }
        } else {
            this.overflowOptions = [];
            this.options = this.screenData.options;
        }

        if (this.screenData.overflowButton) {
            this.keybindingZoneService.removeKeybinding(this.screenData.overflowButton);

            this.keybindingZoneService.getKeyDownEvent(this.screenData.overflowButton.keybind).pipe(
                takeUntil(merge(this.beforeScreenDataUpdated$, this.destroyed$))
            ).subscribe(() => this.openKebabMenu());
        }
    }

    onOptionClick(actionItem: IActionItem): void {
        if (this.optionClick.observers.length > 0) {
            this.optionClick.emit(actionItem);
        } else {
            this.doAction(actionItem);
        }
    }

    public openKebabMenu() {
        if (this.dialog.openDialogs.length < 1 && !this.actionService.actionBlocked()) {
            this.dialogRef = this.dialog.open(KebabMenuComponent, {
                data: {
                    menuItems: this.overflowOptions,
                    payload: null,
                    disableClose: false,
                    autoFocus: false,
                    restoreFocus: false
                },
                panelClass: this.overflowPanelClass,
                width: this.overflowPanelWidth,
                autoFocus: false
            });

            this.subscriptions.add(this.dialogRef.afterClosed().subscribe(result => {
                if (result) {
                    this.optionClick.emit(result);
                }
                this.focusService.restoreInitialFocus();
            }));
        }
    }

    ngOnDestroy() {
        if (this.dialogRef) {
            this.dialogRef.close();
        }
        super.ngOnDestroy();
    }
}
