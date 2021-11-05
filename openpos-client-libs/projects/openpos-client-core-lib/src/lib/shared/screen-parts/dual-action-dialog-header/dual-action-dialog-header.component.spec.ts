import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {Subscription} from 'rxjs';
import {ElectronService} from 'ngx-electron';
import {CLIENTCONTEXT} from '../../../core/client-context/client-context-provider.interface';
import {TimeZoneContext} from '../../../core/client-context/time-zone-context';
import {ActionService} from '../../../core/actions/action.service';
import {KeyPressProvider} from '../../providers/keypress.provider';
import {validateExist, validateIcon, validateText} from '../../../utilites/test-utils';
import {DualActionDialogHeaderComponent} from './dual-action-dialog-header.component';
import {IActionItem} from '../../../core/actions/action-item.interface';
import {DialogHeaderInterface} from '../dialog-header/dialog-header.interface';
import { MockComponent } from 'ng-mocks';
import { IconComponent } from '../../components/icon/icon.component';

class MockActionService {}
class MockMatDialog {}
class MockKeyPressProvider {
    subscribe(): Subscription {
        return new Subscription();
    }
}
class MockElectronService {}
class ClientContext {}

describe('DualActionDialogHeaderComponent', () => {
    let component: DualActionDialogHeaderComponent;
    let fixture: ComponentFixture<DualActionDialogHeaderComponent>;

    describe('shared', () => {
        beforeEach( () => {
            TestBed.configureTestingModule({
                imports: [ HttpClientTestingModule],
                declarations: [
                    DualActionDialogHeaderComponent,
                    MockComponent(IconComponent)
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: KeyPressProvider, useClass: MockKeyPressProvider },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {}},
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext}
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(DualActionDialogHeaderComponent);
            component = fixture.componentInstance;

            component.screenData = {
                backButton: { } as IActionItem,
                headerIcon: 'header-icon',
                headerText: 'My Header'
            } as DialogHeaderInterface;
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('template', () => {
            it('has a text header', () => {
                validateText(fixture, '.header', component.screenData.headerText);
            });
            it('has a profile header icon', () => {
                validateIcon(fixture, '.screen-icon', component.screenData.headerIcon);
            });
            it('has a close button', () => {
                validateExist(fixture, '.back-button');
            });
        });
    });

});
