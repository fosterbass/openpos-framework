import { ActionService } from '../../actions/action.service';
import { ScreenDirective } from './../../../shared/directives/screen.directive';
import { IScreen } from '../../../shared/components/dynamic-screen/screen.interface';
import { Component, OnDestroy, ViewChild, ComponentRef, ComponentFactory } from '@angular/core';
import { ScreenCreatorService } from '../../services/screen-creator.service';
import { KeybindingZoneService } from '../../keybindings/keybinding-zone.service';
import { KeybindingZoneScreenService } from '../../keybindings/keybinding-zone-screen.service';
import { MessageTypes } from '../../messages/message-types';

@Component({
    selector: 'app-dialog-content',
    templateUrl: './dialog-content.component.html',
    styleUrls: ['./dialog-content.component.scss'],
    providers: [ActionService, KeybindingZoneService, KeybindingZoneScreenService]
})
export class DialogContentComponent implements OnDestroy, IScreen {


    @ViewChild(ScreenDirective, { static: true }) host: ScreenDirective;

    private currentScreenRef: ComponentRef<IScreen>;

    private content: IScreen;

    constructor(private screenCreator: ScreenCreatorService,
                private keybindingZoneService: KeybindingZoneService,
                private keybindingZoneScreenService: KeybindingZoneScreenService) {
        this.keybindingZoneScreenService.start(MessageTypes.DIALOG);
    }

    public installScreen(screenComponentFactory: ComponentFactory<IScreen>): void {
        const viewContainerRef = this.host.viewContainerRef;
        viewContainerRef.clear();
        if (this.currentScreenRef) {
            this.currentScreenRef.destroy();
        }
        this.currentScreenRef = this.screenCreator.createScreenComponent(screenComponentFactory, viewContainerRef);
        this.content = this.currentScreenRef.instance;
    }

    show(screen: any): void {
        this.content.show(screen);
    }

    ngOnDestroy(): void {
        if (this.currentScreenRef) {
            this.currentScreenRef.destroy();
        }
    }

}
