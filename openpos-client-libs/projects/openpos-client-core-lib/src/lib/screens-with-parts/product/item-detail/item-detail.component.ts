import { map } from 'rxjs/operators';
import { UIDataMessageService } from '../../../core/ui-data-message/ui-data-message.service';
import { BuddyStoreInterface } from '../buddy-store.interface';
import { ItemDetailInterface } from './item-detail.interface';
import { ScreenComponent } from '../../../shared/decorators/screen-component.decorator';
import { Component, Injector, Optional } from '@angular/core';
import { PosScreenDirective } from '../../pos-screen/pos-screen.component';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { Observable } from 'rxjs';
import { BasicProductOptionPartComponent } from '../option-components/basic-product-option-part/basic-product-option-part';
import { SwatchProductOptionPartComponent } from '../option-components/swatch-product-option-part/swatch-product-option-part.component';
import { ProductOptionInterface } from '../product-option.interface';
import { OPTION_NAME } from '../item-detail-option';
import { IInventoryDetail } from '../inventory-detail.interface';

@ScreenComponent({
    name: 'ItemDetail'
})
@Component({
    selector: 'app-item-detail',
    templateUrl: './item-detail.component.html',
    styleUrls: ['./item-detail.component.scss'],
})
export class ItemDetailComponent extends PosScreenDirective<ItemDetailInterface> {
    isMobile: Observable<boolean>;
    carouselSize: string;

    optionComponents = new Map();

    buddyStores$: Observable<BuddyStoreInterface[]>;
    buddyStoresOnline$: Observable<boolean>;
    inventoryMessage$: Observable<IInventoryDetail>;

    constructor(@Optional() private injector: Injector, media: OpenposMediaService, private dataMessageService: UIDataMessageService) {
        super(injector);
        this.isMobile = media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, false],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));

        this.isMobile.subscribe(mobile => {
            if (mobile) {
                this.carouselSize = 'sm';
            } else {
                this.carouselSize = 'md';
            }
        });
    }

    buildScreen() {
        this.optionComponents.clear();

        if (this.screen.productOptionsComponents) {
            this.screen.productOptionsComponents.forEach(value => {
                const injector = Injector.create({
                    providers: [{ provide: OPTION_NAME, useValue: value.name }],
                    parent: this.injector
                });

                this.optionComponents.set(injector, this.getComponentFromOptionType(value));
            });
        }

        this.buddyStores$ = this.dataMessageService.getData$(this.screen.buddyStoreProviderKey);
        this.buddyStoresOnline$ = this.buddyStores$
            .pipe(map(stores => stores != null && stores !== undefined));
        this.inventoryMessage$ = this.dataMessageService.getData$(this.screen.inventoryMessageProviderKey)
            .pipe(map(value => value != null ? value[0] : null));
        this.screen.imageUrls = [].concat(this.screen.imageUrls);
    }

    getComponentFromOptionType(productOption: ProductOptionInterface) {
        switch (productOption.type) {
            case 'basicProductOption':
                return BasicProductOptionPartComponent;
            case 'swatchProductOption':
                return SwatchProductOptionPartComponent;
        }

    }
}
