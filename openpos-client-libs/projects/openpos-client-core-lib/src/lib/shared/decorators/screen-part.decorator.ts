import { Type } from '@angular/core';

export interface ScreenPartProps {
    name: string;
}

export function ScreenPart(config: ScreenPartProps) {
    return <T extends Type<any>>(target: T) => {
        target.prototype.screenPartName = config.name;
    };
}
