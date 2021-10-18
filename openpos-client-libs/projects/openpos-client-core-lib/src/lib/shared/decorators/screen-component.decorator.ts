import { Type } from '@angular/core';
import { ScreenService } from '../../core/services/screen.service';

export interface ScreenProps {
    name: string;
}

export function ScreenComponent(config: ScreenProps) {
    return <T extends Type<any>>(target: T) => {
        ScreenService.screens.set(config.name, target);
    };
}
