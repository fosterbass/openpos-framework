import {
    Component,
    Input,
} from '@angular/core';
import { Observable } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';

@Component({
    selector: 'app-image-carousel',
    templateUrl: './image-carousel.component.html',
    styleUrls: ['./image-carousel.component.scss']
})
export class ImageCarouselComponent {
    @Input()
    get imgUrls(): string[] {
        return this._imgUrls;
    }

    set imgUrls(value: string[]) {
        if (!value) {
            value = new Array<string>();
        }

        this.displayImageUrls = this._imgUrls = value;

        this.selectImage(0);
    }

    @Input()
    altImageUrl?: string;

    @Input()
    altImageText?: string;

    @Input()
    verticalThumbprints = true;

    displayImageUrls: string[];
    selectedImageUrl?: string;

    private _imgUrls = new Array<string>();

    isMobile$: Observable<boolean>;

    constructor(private mediaService: OpenposMediaService) {
            this.isMobile$ = mediaService.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, false],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));
    }

    selectImage(index: number) {
        if (this.displayImageUrls.length > 0) {
            index = Math.max(0, Math.min(index, this.displayImageUrls.length - 1));
            this.selectedImageUrl = this.displayImageUrls[index];
        } else {
            this.selectedImageUrl = undefined;
        }
    }

    onThumbnailError(index: number) {
        const url = this.displayImageUrls[index];

        this.displayImageUrls.splice(index, 1);

        if (this.selectedImageUrl === url) {
            if (this.displayImageUrls.length > 0) {
                this.selectedImageUrl = this.displayImageUrls[0];
            } else {
                this.selectedImageUrl = undefined;
            }
        }
    }

    onSelectedImageError() {
        this.selectedImageUrl = undefined;
    }
}
