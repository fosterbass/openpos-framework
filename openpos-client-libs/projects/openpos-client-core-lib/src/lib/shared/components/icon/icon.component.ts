import { Component, ElementRef, Input, OnChanges, OnInit, Renderer2, SimpleChanges } from '@angular/core';
import { IconService } from '../../../core/services/icon.service';
import { Observable, throwError } from 'rxjs';
import { SafeHtml } from '@angular/platform-browser';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { catchError } from 'rxjs/operators';

@Component({
    selector: 'app-icon',
    templateUrl: './icon.component.html',
    styleUrls: ['./icon.component.scss']
})

export class IconComponent implements OnInit, OnChanges {

    @Input()
    iconName: string;

    @Input() iconClass !: string;

    parser = new DOMParser();
    icon: Observable<SafeHtml>;

    isMobile: Observable<boolean>;

    constructor(
        private iconService: IconService, private elementRef: ElementRef,
        private renderer: Renderer2, media: OpenposMediaService) {
        this.isMobile = media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, false],
            [MediaBreakpoints.TABLET_LANDSCAPE, false],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));

        this.isMobile.subscribe(mobile => {
            if (mobile) {
                this.renderer.addClass(this.elementRef.nativeElement, 'mobile');
            } else {
                this.renderer.removeClass(this.elementRef.nativeElement, 'mobile');
            }
        });
    }

    ngOnInit(): void {
        this.renderIcon(null);
    }

    ngOnChanges(changes: SimpleChanges): void {

        if (!this.iconClass) {
            this.iconClass = 'mat-24';
        } else if (this.iconClass === 'none') {
            this.iconClass = null;
        }

        this.renderIcon(changes.iconClass ? changes.iconClass.previousValue : null);
    }

    private renderIcon(previousClasses: string) {
        this.icon = this.iconService.getIconHtml(this.iconName).pipe(
            catchError(err => {
                console.error(`Failed to get icon '${this.iconName}'. Error: ${JSON.stringify(err)}`);
                return throwError(err);
            })
        );
        if (previousClasses) {
            previousClasses.split(' ')
                .filter(className => !!className)
                .forEach(e => this.renderer.removeClass(this.elementRef.nativeElement, e));
        }
        if (this.iconClass) {
            this.iconClass.split(' ')
                .filter(className => !!className)
                .forEach(e => this.renderer.addClass(this.elementRef.nativeElement, e));
        }
    }
}
