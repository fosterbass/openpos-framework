import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MockComponent } from 'ng-mocks';
import { IconButtonComponent } from '../icon-button/icon-button.component';
import { IconComponent } from '../icon/icon.component';
import { CarouselComponent } from './carousel.component';

describe('CarouselComponent', () => {
    let wrapper: TestWrapperComponent;
    let carousel: CarouselComponent;
    let fixture: ComponentFixture<TestWrapperComponent>;

    const backButtonId = '#carouselBackButton';
    const forwardButtonId = '#carouselForwardButton';
    const firstItemId = '#firstItem';
    const secondItemId = '#secondItem';
    const thirdItemId = '#thirdItem';
    const fourthItemId = '#fourthItem';
    const dotContainerId = '#carouselDotsContainer';

    const updateCarouselItems = (items: string[]): void => {
        wrapper.items = items;
        fixture.detectChanges();
        carousel.ngAfterContentInit();
        fixture.detectChanges();
    };
    const isShowing = (selector: string): void => {
        expect(fixture.nativeElement.querySelector(selector)).not.toBeNull();
    };
    const isHidden = (selector: string): void => {
        expect(fixture.nativeElement.querySelector(selector)).toBeNull();
    };
    const navigateBack = (): void => {
        fixture.nativeElement.querySelector('#iconButtonBack').click();
        fixture.detectChanges();
    };
    const navigateForward = (): void => {
        fixture.nativeElement.querySelector('#iconButtonForward').click();
        fixture.detectChanges();
    };
    const isDotActive = (dotIndex: number): void => {
        const dots = fixture.nativeElement.querySelectorAll(dotContainerId + ' span');
        expect(dots[dotIndex].classList).toContain('active');
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [TestWrapperComponent, CarouselComponent, IconButtonComponent, MockComponent(IconComponent)]
        }).compileComponents();
        fixture = TestBed.createComponent(TestWrapperComponent);
        wrapper = fixture.componentInstance;
        wrapper.items = ['first'];
        fixture.detectChanges();
        carousel = fixture.componentInstance.carousel;
    });


    it('renders', () => {
        expect(carousel).toBeDefined();
    });

    describe('navigation buttons', () => {
        it('does not show navigation buttons when there is only one item', () => {
            isHidden(backButtonId);
            isHidden(forwardButtonId);
        });
        it('shows both navigation buttons if there are mutliple items', () => {
            updateCarouselItems(['first', 'second']);
            isShowing(backButtonId);
            isShowing(forwardButtonId);
        });
        it('shows the next item when the forward button is clicked', () => {
            updateCarouselItems(['first', 'second']);
            isShowing(firstItemId);
            isHidden(secondItemId);
            navigateForward();
            isHidden(firstItemId);
            isShowing(secondItemId);

        });
        it('hides the back button when wrapNavigationToBack is false and there are mutliple items', () => {
            carousel.wrapNavigationToBack = false;
            updateCarouselItems(['first', 'second']);
            isHidden(backButtonId);
            isShowing(forwardButtonId);
        });
        it('hides the forward button on the last item when wrapNavigationToFront is false', () => {
            carousel.wrapNavigationToFront = false;
            updateCarouselItems(['first', 'second']);
            navigateForward();
            isHidden(forwardButtonId);
            isShowing(backButtonId);
        });
        it('shows the last item when the back button is clicked on the first item', () => {
            updateCarouselItems(['first', 'second', 'third']);
            isShowing(firstItemId);
            navigateBack();
            isShowing(thirdItemId);
        });
        it('navigating forward and back moves one item at a time', () => {
            updateCarouselItems(['first', 'second', 'third']);
            isShowing(firstItemId);
            navigateForward();
            isShowing(secondItemId);
            navigateBack();
            isShowing(firstItemId);
        });
        it('shows the first item when the forward button is clicked on the last item', () => {
            updateCarouselItems(['first', 'second', 'third']);
            navigateForward();
            navigateForward();
            isShowing(thirdItemId);
            navigateForward();
            isShowing(firstItemId);
        });
    });

    describe('items per slide', () => {
        it('shows mutliple items at a time when itemsPerSlide is increased', () => {
            carousel.itemsPerSlide = 2;
            updateCarouselItems(['first', 'second']);
            isShowing(firstItemId);
            isShowing(secondItemId);
        });
        it('navigates through the slides in chunks when itemsPerSlide is greater than 1', () => {
            carousel.itemsPerSlide = 2;
            updateCarouselItems(['first', 'second', 'third', 'fourth']);
            isShowing(firstItemId);
            isShowing(secondItemId);
            isHidden(thirdItemId);
            navigateForward();
            isHidden(firstItemId);
            isShowing(thirdItemId);
            isShowing(fourthItemId);
        });
        it('shows a less than full chunk at the end when the number of slides is not evenly divisible by itemsPerSlide', () => {
            carousel.itemsPerSlide = 2;
            updateCarouselItems(['first', 'second', 'third']);
            isShowing(firstItemId);
            isShowing(secondItemId);
            isHidden(thirdItemId);
            navigateForward();
            isHidden(firstItemId);
            isHidden(secondItemId);
            isShowing(thirdItemId);
        });
        it('does not show navigation buttons when all items are shown in one chunk', () => {
            carousel.itemsPerSlide = 4;
            updateCarouselItems(['first', 'second', 'third', 'fourth']);
            isShowing(firstItemId);
            isShowing(secondItemId);
            isShowing(thirdItemId);
            isShowing(fourthItemId);
            isHidden(backButtonId);
            isHidden(forwardButtonId);
        });
        it('runs fine with less than one chunk', () => {
            carousel.itemsPerSlide = 4;
            updateCarouselItems(['first', 'second']);
            isShowing(firstItemId);
            isShowing(secondItemId);
            isHidden(backButtonId);
            isHidden(forwardButtonId);
        });
    });

    describe('indicator dots', () => {
        it('does not show dots when there is only one chunk', () => {
            isHidden(dotContainerId);
        });
        it('shows 2 dots for a 2 item carousel', () => {
            updateCarouselItems(['first', 'second']);
            const dots = fixture.nativeElement.querySelectorAll(dotContainerId + ' span');
            expect(dots.length).toBe(2);
        });
        it('shows 2 dots for a 2 chunk carousel', () => {
            carousel.itemsPerSlide = 2;
            updateCarouselItems(['first', 'second', 'third', 'fourth']);
            const dots = fixture.nativeElement.querySelectorAll(dotContainerId + ' span');
            expect(dots.length).toBe(2);
        });
        it('changes the active dot as you navigate', () => {
            updateCarouselItems(['first', 'second', 'third']);
            isDotActive(0);
            navigateForward();
            isDotActive(1);
            navigateForward();
            isDotActive(2);
            navigateForward();
            isDotActive(0);
        });
        it('hides the dots when showIndicators is false', () => {
            carousel.showIndicators = false;
            updateCarouselItems(['first', 'second']);
            isHidden(dotContainerId);
        });
    });
});
@Component({
    selector: 'app-test-component',
    template: `
    <app-carousel *ngIf="items?.length > 0">
        <ng-template *ngFor="let item of items" #carouselItem>
            <div id="{{item}}Item">{{item}}</div>
        </ng-template>
    </app-carousel>`
})
class TestWrapperComponent {
    @ViewChild(CarouselComponent) carousel: CarouselComponent;
    items: string[];
}
