import { Component, Injector, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { BarcodeScanner } from '../../core/platform-plugins/barcode-scanners/barcode-scanner.service';
import { ScanData } from '../../core/platform-plugins/barcode-scanners/scanner';
import { DialogComponent } from '../../shared/decorators/dialog-component.decorator';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { EWalletPaymentDialogInterface } from './e-wallet-payment-dialog.interface';

@DialogComponent({
    name: 'EWalletPaymentDialog'
})
@Component({
    selector: 'app-e-wallet-payment-dialog',
    templateUrl: './e-wallet-payment-dialog.component.html',
    styleUrls: ['./e-wallet-payment-dialog.component.scss']
})
export class EWalletPaymentDialogComponent extends PosScreenDirective<EWalletPaymentDialogInterface> implements OnDestroy {

    private scanServiceSubscription: Subscription;

    constructor(
        injector: Injector,
        public imageScanners: BarcodeScanner
    ) {
        super(injector);
        this.registerScanner();
    }

    ngOnDestroy(): void {
        this.unregisterScanner();
    }

    buildScreen() {
    }

    scan(data: ScanData) {
        this.doAction(this.screen.scan.scanActionName, data);
    }

    private registerScanner() {
        if ((typeof this.scanServiceSubscription === 'undefined' || this.scanServiceSubscription === null)) {
            this.scanServiceSubscription = this.imageScanners.beginScanning().subscribe(scanData => {
                this.doAction(this.screen.scan.scanActionName, scanData);
            });
        }
    }

    private unregisterScanner() {
        if (this.scanServiceSubscription) {
            this.scanServiceSubscription.unsubscribe();
            this.scanServiceSubscription = null;
        }
    }
}
