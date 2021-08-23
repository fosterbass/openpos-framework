import { Component, Injector } from '@angular/core';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { PosScreen } from '../../screens-with-parts/pos-screen/pos-screen.component';

@ScreenComponent({
    name: 'Standby'
})
@Component({
    selector: 'app-standby',
    templateUrl: './standby.component.html',
    styleUrls: ['./standby.component.scss']

})
export class StandbyComponent extends PosScreen<any> {

    constructor(injector: Injector) {
        super(injector);
    }
    
    buildScreen() {
    }
}
