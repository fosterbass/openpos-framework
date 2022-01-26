import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionService } from '../../../../core/actions/action.service';
import { SwatchProductOptionPartComponent } from './swatch-product-option-part.component';
import { ImageService } from '../../../../core/services/image.service';
import { SessionService } from '../../../../core/services/session.service';
import { OpenposMediaService } from '../../../../core/media/openpos-media.service';
import { BehaviorSubject, of } from 'rxjs';
import { MessageProvider } from '../../../../shared/providers/message.provider';
import { SwatchProductOptionPartInterface } from './swatch-product-option-part.interface';
import { UIMessage } from '../../../../core/messages/ui-message';
import { MatSelectModule } from '@angular/material/select';

describe('SwatchProductOptionPartComponent', () => {
    let component: SwatchProductOptionPartComponent;
    let fixture: ComponentFixture<SwatchProductOptionPartComponent>;

    const screenData: SwatchProductOptionPartInterface = {
        swatches: [
            {
                name: 'Blue',
                id: 'blueSwatch',
                imageUrl: 'https://fakeurl.com/blueSwatch'
            },
            {
                name: 'Red',
                id: 'redSwatch',
                imageUrl: 'https://fakeurl.com/redSwatch'
            }
        ],
        selectOptionAction: {
            action: 'action'
        },
        optionPlaceholder: 'placeholder'
    };

    let scopedMessages: BehaviorSubject<UIMessage>;
    const mockService = {
        doAction: () => { },
        observe: () => of({}),
        getScopedMessages$: () => scopedMessages.asObservable(),
        getAllMessages$: () => of({}),
        replaceImageUrl: (image: string): string => image
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [MatSelectModule],
            declarations: [SwatchProductOptionPartComponent],
            providers: [
                { provide: ImageService, useValue: mockService },
                { provide: SessionService, useValue: mockService },
                { provide: OpenposMediaService, useValue: mockService },
                { provide: ActionService, useValue: mockService },
                { provide: MessageProvider, useValue: mockService }
            ]

        }).compileComponents();
        scopedMessages = new BehaviorSubject(JSON.parse(JSON.stringify(screenData)));
        fixture = TestBed.createComponent(SwatchProductOptionPartComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('creates component', () => {
        expect(component).toBeTruthy();
    });

    it('has an img for each swatch with the proper src', () => {
        const images = fixture.nativeElement.querySelectorAll('img');
        expect(images.length).toBe(2);
        expect(images[0].src).toBe(screenData.swatches[0].imageUrl);
        expect(images[1].src).toBe(screenData.swatches[1].imageUrl);
    });
    it('displays the selected swatch as a caption', () => {
        const updatedScreenData: SwatchProductOptionPartInterface = JSON.parse(JSON.stringify(screenData));
        updatedScreenData.selectedOption = 'blueSwatch';
        scopedMessages.next(updatedScreenData as any);
        fixture.detectChanges();
        const caption = fixture.nativeElement.querySelector('caption');
        expect(caption.textContent).toContain('Blue');
    });
    it('shows a dropdown when loading images fails', () => {
        component.onImageLoadFailed();
        fixture.detectChanges();
        const dropdown = fixture.nativeElement.querySelector('mat-select');
        expect(dropdown).toBeTruthy();
        expect(dropdown.textContent).toContain(screenData.optionPlaceholder);
    });
    it('selecting a swatch does the selectOptionAction', () => {
        spyOn(component.actionService, 'doAction').and.callThrough();
        const blueSwatch = fixture.nativeElement.querySelector('img');
        blueSwatch.click();
        expect(component.actionService.doAction).toHaveBeenCalledWith(screenData.selectOptionAction, 'blueSwatch');
    });
    it('clears selectedOptionName when screenData is undefined', () => {
        component.screenData = undefined;
        component.selectedOptionName = 'selected';
        component.screenDataUpdated();
        expect(component.selectedOptionName).toBe('');
    });
    it('clears selectedOptionName when swatches are undefined', () => {
        component.screenData = {} as SwatchProductOptionPartInterface;
        component.selectedOptionName = 'selected';
        component.screenDataUpdated();
        expect(component.selectedOptionName).toBe('');
    });
});
