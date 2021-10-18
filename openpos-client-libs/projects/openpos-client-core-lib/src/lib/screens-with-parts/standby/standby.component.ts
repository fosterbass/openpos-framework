import { Component, Injector } from '@angular/core';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { PosScreenDirective } from '../../screens-with-parts/pos-screen/pos-screen.component';
import { StandByInterface } from './standby.interface';

@ScreenComponent({
    name: 'Standby'
})
@Component({
    selector: 'app-standby',
    templateUrl: './standby.component.html',
    styleUrls: ['./standby.component.scss']

})
export class StandbyComponent extends PosScreenDirective<StandByInterface> {

    constructor(injector: Injector) {
        super(injector);
    }

    buildScreen() {
    }

}
