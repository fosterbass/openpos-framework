import { Component, Input, OnInit, Output, EventEmitter, Optional } from '@angular/core';
import { DeviceService } from '../../../services/device.service';
import { SessionService } from '../../../services/session.service';
import { MatDialogRef } from '@angular/material';

@Component({
  selector: 'app-scan-something',
  templateUrl: './scan-something.component.html',
  styleUrls: ['./scan-something.component.scss']
})
export class ScanSomethingComponent implements OnInit {

  public barcode: string;

  constructor(private session: SessionService, public devices: DeviceService,
    @Optional() public dialogRef: MatDialogRef<ScanSomethingComponent>) { }

  ngOnInit() {
  }

  public onEnter(): void {
    if (this.barcode && this.barcode.length > 0) {
    this.session.onAction('Next', this.barcode);
    this.barcode = '';
    if (this.dialogRef) {
      this.dialogRef.close();
    }
  }
  }

  private filterBarcodeValue(val: string): string {
    if (!val) {
      return val;
    }
    // Filter out extra characters permitted by HTML5 input type=number (for exponentials)
    const pattern = /[e|E|\+|\-|\.]/g;

    return val.toString().replace(pattern, '');
  }

  onBarcodeKeydown(event: KeyboardEvent) {
    if (event.altKey || event.ctrlKey || event.metaKey ) {
      return true;
    }
    const filteredKey = this.filterBarcodeValue(event.key);
    console.log(`[onBarcodeKeydown] filtered key: ${filteredKey}`);
    return filteredKey !== null && filteredKey.length !== 0;
  }

  onBarcodePaste(event: ClipboardEvent) {
    const content = event.clipboardData.getData('text/plain');
    const filteredContent = this.filterBarcodeValue(content);
    if (filteredContent !== content) {
      console.log(`Clipboard data contains invalid characters for barcode, suppressing pasted content '${content}'`);
    }
    return filteredContent === content;
  }
}
