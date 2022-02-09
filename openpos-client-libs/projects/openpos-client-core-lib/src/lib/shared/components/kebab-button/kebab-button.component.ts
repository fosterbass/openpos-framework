import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { merge, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { ActionService } from '../../../core/actions/action.service';
import { KebabMenuComponent } from '../kebab-menu/kebab-menu.component';
import { FocusService } from '../../../core/focus/focus.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';

@Component({
    selector: 'app-kebab-button',
    templateUrl: './kebab-button.component.html',
    styleUrls: ['./kebab-button.component.scss']
})
export class KebabButtonComponent implements OnChanges, OnDestroy {
    private destroyed$ = new Subject();
    private keybindingChanged$ = new Subject();

    @Input()
    menuItems: IActionItem[];

    @Input()
    color?: string;

    @Input()
    iconName = 'KebabMenu';

    @Input()
    iconClass;

    dialogRef: MatDialogRef<KebabMenuComponent>;

    @Input()
        // Some examples:
        // p
        // shift+enter
        // p,shift+enter,escape
    keyBinding: string;

    @Output()
    menuItemClick = new EventEmitter<IActionItem>();

    modalWidth = '35vw';

    constructor(
        protected dialog: MatDialog,
        protected keybindingZoneService: KeybindingZoneService,
        protected focusService: FocusService,
        protected actionService: ActionService,
        private mediaService: OpenposMediaService
    ) {
        this.checkScreenSize();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.keyBinding) {
            this.keybindingChanged$.next();
            this.updateKeybinding();
        }
    }

    updateKeybinding(): void {
        this.keybindingZoneService.getKeyDownEvent(this.keyBinding)
            .pipe(
                takeUntil(merge(this.keybindingChanged$, this.destroyed$))
            ).subscribe(() => this.openKebabMenu());
    }

    ngOnDestroy(): void {
        this.destroyed$.next();
        // Ensure dialog gets closed, if it is still open due
        // to a screen change/refresh while it was open
        this.closeKebabMenu();
    }

    public openKebabMenu() {
        if (this.dialog.openDialogs.length < 1 && !this.actionService.actionBlocked()) {
            this.dialogRef = this.dialog.open(KebabMenuComponent, {
                data: {
                    menuItems: this.menuItems,
                    payload: null,
                    disableClose: false,
                    autoFocus: false,
                    restoreFocus: false
                },
                width: this.modalWidth,
                autoFocus: false
            });

            this.dialogRef.afterClosed().pipe(finalize(() => this.dialogRef = undefined)).subscribe(result => {
                if (result) {
                    this.menuItemClick.emit(result);
                }
                this.focusService.restoreInitialFocus();
            });
        }
    }

    closeKebabMenu(): void {
        if (this.dialogRef) {
            this.dialogRef.close();
        }
    }

    checkScreenSize() {
        this.mediaService.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, '90vw'],
            [MediaBreakpoints.MOBILE_LANDSCAPE, '50vw'],
            [MediaBreakpoints.TABLET_PORTRAIT, '70vw'],
            [MediaBreakpoints.TABLET_LANDSCAPE, '50vw'],
            [MediaBreakpoints.DESKTOP_PORTRAIT, '50vw'],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, '35vw']
        ])).subscribe(res => this.modalWidth = res);
    }
}
