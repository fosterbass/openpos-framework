import { Component, OnInit } from '@angular/core';
import { LockScreenService } from '../../../core/lock-screen/lock-screen.service';
import { OpenposMediaService } from '../../../core/media/openpos-media.service';
import { PersonalizationService } from '../../../core/personalization/personalization.service';
import { PrinterService } from '../../../core/platform-plugins/printers/printer.service';
import { FetchMessageService } from '../../../core/services/fetch-message.service';
import { LocaleService } from '../../../core/services/locale.service';
import { LocationService } from '../../../core/services/location.service';
import { DialogService } from '../../../core/services/dialog.service';
import { StartupController } from '../../startup/startup-controller.service';

@Component({
    selector: 'app-openpos-root',
    templateUrl: './openpos-app.component.html'
})
export class OpenposAppComponent implements OnInit {
    constructor(
        protected personalization: PersonalizationService,
        protected localeService: LocaleService,
        protected locationService: LocationService,
        protected mediaService: OpenposMediaService,
        protected lockScreenService: LockScreenService,
        protected printService: PrinterService,
        protected fetchMessageService: FetchMessageService,
        protected dialogService: DialogService,
        public startupController: StartupController
    ) { }

    async ngOnInit() {
        await this.startupController.beginStartupSequence();
    }
}
