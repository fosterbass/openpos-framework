import { Component, Inject, Injector, OnInit, Optional } from '@angular/core';
import { ImageService } from '../../../../core/services/image.service';
import { ScreenPart } from '../../../../shared/decorators/screen-part.decorator';
import { ScreenPartComponent } from '../../../../shared/screen-parts/screen-part';
import { OPTION_NAME } from '../../item-detail-option';
import { SwatchProductOptionPartInterface } from './swatch-product-option-part.interface';

@ScreenPart({
    name: 'swatchProductOption'
})
@Component({
    selector: 'app-swatch-product-options-part',
    templateUrl: './swatch-product-option-part.component.html',
    styleUrls: ['./swatch-product-option-part.scss']
})
export class SwatchProductOptionPartComponent extends ScreenPartComponent<SwatchProductOptionPartInterface> implements OnInit {
    selectedOptionName: string;
    useImageSwatch = true;

    constructor(
        @Optional() injector: Injector,
        @Optional() @Inject(OPTION_NAME) private optionName: string,
        private imageService: ImageService
    ) {
        super(injector);
    }


    ngOnInit(): void {
        this.screenPartName = 'swatchProductOption' + this.optionName;
        super.ngOnInit();
    }

    screenDataUpdated() {
        if (this.screenData?.swatches) {
            const selectedSwatch = this.screenData.swatches.find(value => value.id === this.screenData.selectedOption);

            if (selectedSwatch) {
                this.selectedOptionName = selectedSwatch.name;
            } else {
                this.selectedOptionName = '';
            }

            this.screenData.swatches.forEach(swatch => {
                swatch.imageUrl = this.imageService.replaceImageUrl(swatch.imageUrl);
            });
        } else {
            this.selectedOptionName = '';
        }

        console.log(this.selectedOptionName);
    }

    selectOption(swatchId: string) {
        console.log('selected', swatchId);
        const data = {optionId: this.screenData.optionId, value: swatchId};
        this.doAction(this.screenData.selectOptionAction, data);
    }

    onImageLoadFailed() {
        this.useImageSwatch = false;
    }
}
