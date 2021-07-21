import { TestBed, ComponentFixture } from '@angular/core/testing';
import { SaleComponent } from './sale.component';
import { MatDialog, MatBottomSheet } from '@angular/material';
import { OpenposMediaService } from '../../core/media/openpos-media.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { KeyPressProvider } from '../../shared/providers/keypress.provider';
import { NO_ERRORS_SCHEMA } from '@angular/core'
import { ActionService } from '../../core/actions/action.service';
import { ElectronService } from 'ngx-electron';
import { ImageUrlPipe } from '../../shared/pipes/image-url.pipe';
import { TimeZoneContext } from '../../core/client-context/time-zone-context';
import { CLIENTCONTEXT } from '../../core/client-context/client-context-provider.interface';
import { SaleInterface } from './sale.interface';
import { Subscription, Observable, of } from 'rxjs';
import {validateDoesNotExist, validateExist} from "../../utilites/test-utils";
import {ISellItem} from "../../core/interfaces/sell-item.interface";

class MockMatDialog {};
class MockActionService {};
class MockMatBottomSheet {};
class MockKeyPressProvider {
    subscribe(): Subscription {
        return new Subscription();
    }
};
class MockElectronService {};
class ClientContext {};

describe('SaleComponent', () => {
    let component: SaleComponent;
    let fixture: ComponentFixture<SaleComponent>;
    let openposMediaSerivce: OpenposMediaService;

    describe('non mobile', () => {
        class MockOpenposMediaServiceMobile {
            observe(): Observable<boolean> {
                return of(false);
            }
        };
        beforeEach( () => {
            TestBed.configureTestingModule({
                imports: [HttpClientTestingModule],
                declarations: [
                    SaleComponent,
                    ImageUrlPipe
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog},
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobile },
                    { provide: MatBottomSheet, useClass: MockMatBottomSheet},
                    { provide: KeyPressProvider, useClass: MockKeyPressProvider },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {}},
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext}
                ],
                schemas: [
                    NO_ERRORS_SCHEMA,
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(SaleComponent);
            component = fixture.componentInstance;
            component.screen = {} as SaleInterface;
            component.screen.orders = [];
            openposMediaSerivce = TestBed.get(OpenposMediaService);
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('component', () => {

        });

        describe('template', () => {

        });
    });

    describe('mobile', () => {
        class MockOpenposMediaServiceMobile {
            observe(): Observable<boolean> { return of(true); }
        };
        beforeEach( () => {
            TestBed.configureTestingModule({
                imports: [HttpClientTestingModule],
                declarations: [SaleComponent, ImageUrlPipe],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog},
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobile },
                    { provide: MatBottomSheet, useClass: MockMatBottomSheet},
                    { provide: KeyPressProvider, useClass: MockKeyPressProvider },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {}},
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext}
                ],
                schemas: [NO_ERRORS_SCHEMA]
            }).compileComponents();
            fixture = TestBed.createComponent(SaleComponent);
            component = fixture.componentInstance;
            component.items = of([{} as ISellItem]);
            component.screen = {} as SaleInterface;
            component.screen.orders = [];
            openposMediaSerivce = TestBed.get(OpenposMediaService);
            fixture.detectChanges();
        });

        describe('template', () => {
            it('does not show the status strip when no items are on the transaction', () => {
                component.items = of([]);
                fixture.detectChanges();
                validateDoesNotExist(fixture, '.sale-body-mobile app-status-strip');
            });

            it('shows the status strip when items are on the transaction', () => {
                validateExist(fixture, '.sale-body-mobile app-status-strip');
            });
        });
    });
});