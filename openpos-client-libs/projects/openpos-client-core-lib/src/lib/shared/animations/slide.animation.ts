import {
    animate,
    animation,
    style,
    transition,
    trigger,
    useAnimation
} from '@angular/animations';

export const slideInAnimation = animation([
    style({ transform: 'translate{{axis}}({{start}})', opacity: 0 }),
    animate('400ms cubic-bezier(0.25, 0.8, 0.25, 1)',
        style({ transform: 'translate{{axis}}(0%)', opacity: 1 })
    )
]);

export const slideOutAnimation = animation([
    animate('400ms cubic-bezier(0.25, 0.8, 0.25, 1)',
        style({ transform: 'translate{{axis}}({{end}})', opacity: 0 })
    )
]);

export const slideLeftAnimationTrigger = trigger('slideLeft', [
    transition(':enter', useAnimation(slideInAnimation, { params: { axis: 'X', start: '100%' } })),
    transition(':leave', useAnimation(slideOutAnimation, { params: { axis: 'X', end: '-100%' } }))
]);

export const slideLeftRightAnimationTrigger = trigger('slideLeftRight', [
    transition(':enter', useAnimation(slideInAnimation, { params: { axis: 'X', start: '100%' } })),
    transition(':leave', useAnimation(slideOutAnimation, { params: { axis: 'X', end: '100%' } }))
]);

export const slideUpDownAnimationTrigger = trigger('slideUpDown', [
    transition(':enter', useAnimation(slideInAnimation, { params: { axis: 'Y', start: '100%' } })),
    transition(':leave', useAnimation(slideOutAnimation, { params: { axis: 'Y', end: '100%' } }))
]);

export const slideDownUpAnimationTrigger = trigger('slideDownUp', [
    transition(':enter', useAnimation(slideInAnimation, { params: { axis: 'Y', start: '-100%' } })),
    transition(':leave', useAnimation(slideOutAnimation, { params: { axis: 'Y', end: '-100%' } }))
]);
