import { ScreenService } from '../../core/services';

export interface ScreenProps {
    name: string;
}

export function ScreenComponent( config: ScreenProps ) {
    return function(target) {
        ScreenService.screens.set(config.name, target);
    };
}
