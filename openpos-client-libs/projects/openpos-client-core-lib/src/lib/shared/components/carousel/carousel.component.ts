import {
    Component,
    ContentChildren,
    QueryList,
    TemplateRef,
    AfterContentInit,
    Input
} from '@angular/core';

@Component({
    selector: 'app-carousel',
    templateUrl: './carousel.component.html',
    styleUrls: ['./carousel.component.scss']
})
export class CarouselComponent implements AfterContentInit {
    @Input() navigationArrowsSize = 'lg';
    @Input() carouselItemClass = '';
    @Input() showIndicators = true;
    @Input() wrapNavigationToFront = true;
    @Input() wrapNavigationToBack = true;
    @Input() itemsPerSlide = 1;

    @ContentChildren('carouselItem') items: QueryList<TemplateRef<any>>;
    sections: TemplateRef<any>[][];
    currentSection: TemplateRef<any>[];
    index = 0;

    ngAfterContentInit(): void {
        this.sections = [];
        for (let i = 0; i < this.items.length; i += this.itemsPerSlide) {
            const chunk = this.items.toArray().slice(i, i + this.itemsPerSlide);
            this.sections.push(chunk);
        }
        this.currentSection = this.sections[this.index];
    }

    moveForward(): void {
        this.index++;
        if (this.index >= this.sections.length) {
            this.index = 0;
        }
        this.currentSection = this.sections[this.index];
    }

    moveBackward(): void {
        this.index--;
        if (this.index < 0) {
            this.index = this.sections.length - 1;
        }
        this.currentSection = this.sections[this.index];
    }

    forwardEnabled(): boolean {
        return (this.wrapNavigationToFront || this.index < this.sections.length - 1) && this.sections.length > 1;
    }

    backwardEnabled(): boolean {
        return (this.wrapNavigationToBack || this.index > 0) && this.sections.length > 1;
    }

    isDotActive(dotIndex: number): boolean {
        return dotIndex === this.index;
    }

    getCarouselColumnSize(): number {
        return this.forwardEnabled() || this.backwardEnabled() ? 10 : 12;
    }

    getItemColumnSize(): number {
        let biggestSection = 0;
        for (const section of this.sections) {
            if (section.length > biggestSection) {
                biggestSection = section.length;
            }
        }
        return Math.trunc(12 / biggestSection);
    }
}
