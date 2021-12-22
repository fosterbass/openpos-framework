import { Observable, Subject } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';
import { Component, OnDestroy } from '@angular/core';
import { IScreen } from '../../../shared/components/dynamic-screen/screen.interface';
import { ScreenComponent } from '../../../shared/decorators/screen-component.decorator';
import { SplashScreen } from '../../services/splash-screen.service';

@ScreenComponent({
    name: 'SplashScreen'
})
@Component({
    selector: 'app-splash-screen',
    templateUrl: './splash-screen.component.html',
    styleUrls: ['./splash-screen.component.scss']
})
export class SplashScreenComponent implements IScreen, OnDestroy {
    message$: Observable<string | null>;

    private destroy$ = new Subject();

    constructor(splashScreen: SplashScreen) {
        this.message$ = splashScreen.observeMessage().pipe(
            takeUntil(this.destroy$)
        );
    }

    show(screen: any): void {
    }

    ngOnDestroy(): void {
        this.destroy$.next();
    }

}
