import { OverlayContainer } from '@angular/cdk/overlay';
import { Injectable } from '@angular/core';

@Injectable()
export class ModalOverlayContainer extends OverlayContainer {
    public setContainerElement(elemet: HTMLElement): void {
        this._containerElement = elemet;
    }
}
