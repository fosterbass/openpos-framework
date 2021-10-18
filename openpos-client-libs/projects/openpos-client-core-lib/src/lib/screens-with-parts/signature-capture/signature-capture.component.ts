import { Component, AfterViewInit, HostListener, Injector } from '@angular/core';
import 'signature_pad';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CONFIGURATION } from '../../configuration/configuration';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { SignatureCaptureInterface } from './signature-capture.interface';


@ScreenComponent({
  name: 'SignatureCapture'
})
@Component({
  selector: 'app-signature-capture',
  templateUrl: './signature-capture.component.html',
  styleUrls: ['./signature-capture.component.scss']
})
export class SignatureCaptureComponent extends PosScreenDirective<SignatureCaptureInterface> implements AfterViewInit {

  static readonly DEFAULT_MEDIA_TYPE = 'image/jpeg';

  protected initialized = false;
  protected signaturePad: SignaturePad;
  protected canvas: HTMLCanvasElement = null;
  protected wrapper: HTMLElement;

  constructor(public snackBar: MatSnackBar, injector: Injector) {
    super(injector);
  }

  buildScreen() { }

  ngAfterViewInit(): void {
    this.initialized = true;
    this.wrapper = document.getElementById('signature-pad');
    this.canvas = this.wrapper.querySelector('canvas');
    this.canvas.height = this.wrapper.clientHeight;
    this.canvas.width = this.wrapper.clientWidth;
    this.signaturePad = new SignaturePad(this.canvas);
    this.onResizeCanvas(null);
  }

  @HostListener('window:resize', ['$event'])
  onResizeCanvas(evt: Event) {

    const newWidth = this.wrapper.clientWidth;
    const newHeight = this.wrapper.clientHeight;

    const tempCanvas = document.createElement('canvas');
    const tempContext = tempCanvas.getContext('2d');
    const canvasContext = this.canvas.getContext('2d');

    tempCanvas.width = newWidth;
    tempCanvas.height = newHeight;
    tempContext.fillStyle = 'white'; // TODO: this.canvas.getAttribute('backgroundColor');
    tempContext.fillRect(0, 0, newWidth, newHeight);
    tempContext.drawImage(this.canvas, 0, 0);

    // Don't resize original canvas until after the image has been copied
    this.canvas.width = newWidth;
    this.canvas.height = newHeight;

    canvasContext.drawImage(tempCanvas, 0, 0);
  }

  onClearSignature(): void {
    this.signaturePad.clear();
  }

  onSaveSignature(): void {
    if (this.signaturePad.isEmpty()) {
      console.info('Signature is empty');
      return;
    }
    const mediaType: string = this.screen.signatureMediaType ?
      this.screen.signatureMediaType : SignatureCaptureComponent.DEFAULT_MEDIA_TYPE;

    const dataUrl: string | null = this.signaturePad.toDataURL(mediaType);
    const dataPoints = this.signaturePad.toData();

    let encodedImage: string | null = null;
    if (dataUrl) {
      const matches: RegExpMatchArray | null = dataUrl.match(/^data:.+\/(.+);base64,(.*)$/);
      encodedImage = matches && matches.length > 2 ? matches[2] : null;
    }
    const signatureData: ISignature = {
      pointGroups: dataPoints,
      mediaType,
      base64EncodedImage: encodedImage
    };

    if (!this.isSignatureSizeValid(signatureData)) {
      this.snackBar.open('Signature is too large, please try again', 'Dismiss', {
        duration: 8000, verticalPosition: 'top'
      });
      return;
    }

    this.doAction(this.screen.saveAction.action, signatureData);
  }

  protected isSignatureSizeValid(sigData: ISignature) {

    if (CONFIGURATION.maxSignaturePoints >= 0) {
      let totalSignaturePoints = 0;
      if (sigData.pointGroups) {
        sigData.pointGroups.forEach(pArray => totalSignaturePoints += pArray.length);
        console.info(`Total signature points: ${totalSignaturePoints}`);
      }

      if (totalSignaturePoints > CONFIGURATION.maxSignaturePoints) {
        console.info(`Signature point count of ${totalSignaturePoints} exceeds the CONFIGURATION.maxSignaturePoints of ` +
          `${CONFIGURATION.maxSignaturePoints}`);
        return false;
      }
    }

    if (CONFIGURATION.maxResponseSizeBytes >= 0) {
      const signatureResponseSize = JSON.stringify(sigData).length;
      console.info(`Signature response size: ${signatureResponseSize}`);
      if (signatureResponseSize > CONFIGURATION.maxResponseSizeBytes) {
        console.info(`Signature response size of ${signatureResponseSize} exceeds the CONFIGURATION.maxResponseSizeBytes of ` +
          `${CONFIGURATION.maxResponseSizeBytes}`);
        return false;
      }
    }

    return true;
  }
}

export interface ISignature {
  pointGroups: IPoint[][];
  mediaType: string;
  base64EncodedImage: string | null;
}

export interface IPoint {
  x: number;
  y: number;
  time: number;
  color?: string;
}
