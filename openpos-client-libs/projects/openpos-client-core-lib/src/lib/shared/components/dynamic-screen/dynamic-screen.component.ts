import { Component, OnInit, ViewChild } from '@angular/core';
import { ActionService } from '../../../core/actions/action.service';
import { MessageProvider } from '../../providers/message.provider';
import { ToastContainerDirective, ToastrService } from 'ngx-toastr';
import { SessionService } from '../../../core/services/session.service';
import { WatermarkMessage } from '../../../core/messages/watermark-message';
import { MessageTypes } from '../../../core/messages/message-types';
import { ConfigurationService } from '../../../core/services/configuration.service';
import { UIConfigMessage } from '../../../core/messages/ui-config-message';
import { UIMessage } from '../../../core/messages/ui-message';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { KeybindingZoneScreenService } from '../../../core/keybindings/keybinding-zone-screen.service';

@Component({
    selector: 'app-dynamic-screen',
    templateUrl: './dynamic-screen.component.html',
    styleUrls: ['./dynamic-screen.component.scss'],
    providers: [MessageProvider, ActionService, KeybindingZoneService, KeybindingZoneScreenService]
})
export class DynamicScreenComponent implements OnInit {
    @ViewChild(ToastContainerDirective, { static: true })
    toastContainer: ToastContainerDirective;
    showWatermark = false;
    watermarkMessage: string;
    showStatusBarAppId = true;
    showStatusBarState = true;

    constructor(
        private configuration: ConfigurationService,
        private messageProvider: MessageProvider,
        private toastrService: ToastrService,
        private sessionService: SessionService,
        private keybindingZoneService: KeybindingZoneService,
        private keybindingZoneScreenService: KeybindingZoneScreenService
    ) {
        this.keybindingZoneScreenService.start(MessageTypes.SCREEN);
        this.messageProvider.setMessageType(MessageTypes.SCREEN);
        this.sessionService.getMessages(MessageTypes.WATERMARK).subscribe((message: WatermarkMessage) => {
            this.showWatermark = true;
            this.watermarkMessage = message?.screenMessage;
        });
        this.sessionService.getMessages(MessageTypes.HIDE_WATERMARK).subscribe(() => {
            this.showWatermark = false;
            this.watermarkMessage = '';
        });
        this.configuration.getConfiguration<UIConfigMessage>('uiConfig').subscribe(uiConfig => {
            this.showStatusBarAppId = uiConfig?.showStatusBar === 'true';
        });
        this.messageProvider.getScopedMessages$<UIMessage>().subscribe(screen => {
            this.showStatusBarState = screen?.showStatusBar;
        });
    }

    ngOnInit() {
        this.toastrService.overlayContainer = this.toastContainer;
    }

    getDynamicClasses(): string {
        let classes = this.configuration.theme$.getValue();
        if (!(this.showStatusBarAppId && this.showStatusBarState)) {
            classes += ' no-status-bar';
        }
        return classes;
    }
}
