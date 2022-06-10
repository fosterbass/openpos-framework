import { AfterContentChecked, ChangeDetectorRef, Component, Injector, Optional, TemplateRef, ViewChild } from '@angular/core';
import { Observable } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { UIDataMessageService } from '../../../core/ui-data-message/ui-data-message.service';
import { ScreenComponent } from '../../../shared/decorators/screen-component.decorator';
import { PosScreenDirective } from '../../pos-screen/pos-screen.component';
import { BuddyStoreInterface } from '../buddy-store.interface';
import { BasicProductOptionPartComponent } from '../option-components/basic-product-option-part/basic-product-option-part';
import { SwatchProductOptionPartComponent } from '../option-components/swatch-product-option-part/swatch-product-option-part.component';
import { ProductOptionInterface } from '../product-option.interface';
import { ProductDetailInterface } from './product-detail.interface';
import { OPTION_NAME } from '../item-detail-option';
import { map } from 'rxjs/operators';
import { ITabContent } from '../../../shared/components/tabbed-content-card/tab-content.interface';
import { IDataTableRow } from '../../../shared/components/grid-table/data-table-row.interface';
import { IInventoryDetail } from '../inventory-detail.interface';
import { AvailabilityType } from './availability-type.enum';

@ScreenComponent({
    name: 'ProductDetail'
})
@Component({
    selector: 'app-product-detail',
    templateUrl: './product-detail.component.html',
    styleUrls: ['./product-detail.component.scss'],
})
export class ProductDetailComponent extends PosScreenDirective<ProductDetailInterface> implements AfterContentChecked {

    isMobile: Observable<boolean>;

    optionComponents = new Map();

    buddyStoreInventory$: Observable<IDataTableRow[]>;
    buddyStores$: Observable<BuddyStoreInterface[]>;
    buddyStoresOnline$: Observable<boolean>;


    @ViewChild('info') infoTemplate: TemplateRef<any>;
    @ViewChild('tabContent') tabContentTemplate: TemplateRef<any>;

    public selectedTabValue: string;
    public selectedTabContent: ITabContent;
    columns: string[];

    constructor(@Optional() private injector: Injector, media: OpenposMediaService,
                private dataMessageService: UIDataMessageService,
                private changeDetection: ChangeDetectorRef) {
        super(injector);
        this.isMobile = media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, false],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));
    }

    ngAfterContentChecked() {
        this.changeDetection.detectChanges();
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

        if (this.screen.productInventory) {
            this.buddyStores$ = this.dataMessageService.getData$(this.screen.productInventory.buddyStoreProviderKey);
            this.buddyStoreInventory$ = this.buddyStores$.pipe(map(stores =>
                stores.map(s => ({columns: [[s.storeName], [s.storeCount]]}))));
            this.buddyStoresOnline$ = this.buddyStores$
                .pipe(map(stores => stores != undefined));
        }

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

    getAvailabilityStyle() {
        return this.screen.availabilityType === AvailabilityType.AVAILABLE ? 'stock' : 'not-available';
    }

    public getSelectedTabTemplate(): TemplateRef<any> {
        if (this.selectedTabValue === 'info') {
          return this.infoTemplate;
        } else {
          return this.tabContentTemplate;
        }
    }

    public updateSelectedTab(val: string): void {
        if (val) {
            this.selectedTabValue = val;
            const tabContent = this.screen.tabContents.filter(t => t.tabId === val);
            if (tabContent?.length > 0) {
                this.selectedTabContent = tabContent[0];
            }
        } else {
            this.selectedTabContent = undefined;
        }
    }

    public getTabContentForTabId(tab: string): ITabContent {
        const tabContent = this.screen.tabContents.filter(t => t.tabId === tab);
        if (tabContent && tabContent.length > 0) {
            return tabContent[0];
        }
        return null;
    }
}
