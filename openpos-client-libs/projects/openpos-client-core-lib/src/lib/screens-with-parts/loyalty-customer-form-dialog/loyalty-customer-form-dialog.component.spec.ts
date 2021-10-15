import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActionService } from '../../core/actions/action.service';
import { OpenposMediaService } from '../../core/media/openpos-media.service';
import { LoyaltyCustomerFormDialogComponent } from './loyalty-customer-form-dialog.component';
import { LoyaltyCustomerFormInterface } from './loyalty-customer-form.interface';
import { CLIENTCONTEXT } from '../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../core/client-context/time-zone-context';
import { ElectronService } from 'ngx-electron';
import { KeyPressProvider } from '../../shared/providers/keypress.provider';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { IForm } from '../../core/interfaces/form.interface';
import { FormBuilder } from '../../core/services/form-builder.service';

describe('LoyaltyCustomerFormDialogComponent', () => {
    let component: LoyaltyCustomerFormDialogComponent;
    let fixture: ComponentFixture<LoyaltyCustomerFormDialogComponent>;
    let formBuilder: FormBuilder;

    const mockService = {
        observe: () => false,
        doAction: () => { },
        buildFormPayload: () => mockForm,
        group: () => ({})
    };
    class ClientContext { }

    const mockForm: IForm = {
        formElements: [
            { id: 'findThisId', label: 'first' },
            { id: 'findThisId', label: 'second' }
        ],
        requiresAtLeastOneValue: true,
        formErrors: [],
        name: 'FakeForm'
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            declarations: [
                LoyaltyCustomerFormDialogComponent
            ],
            providers: [
                { provide: ActionService, useValue: mockService },
                { provide: MatDialog, useValue: mockService },
                { provide: OpenposMediaService, useValue: mockService },
                { provide: KeyPressProvider, useValue: mockService },
                { provide: ElectronService, useValue: mockService },
                { provide: FormBuilder, useValue: mockService },
                { provide: ClientContext, useValue: {} },
                { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
            ],
            schemas: [
                NO_ERRORS_SCHEMA,
            ]
        }).compileComponents();
        fixture = TestBed.createComponent(LoyaltyCustomerFormDialogComponent);
        component = fixture.componentInstance;
        component.screen = { form: JSON.parse(JSON.stringify(mockForm)) } as LoyaltyCustomerFormInterface;
        formBuilder = TestBed.inject(FormBuilder);
        fixture.detectChanges();
    });

    it('renders', () => {
        expect(component).toBeDefined();
    });

    describe('getFormElementById(formElementId: string)', () => {
        it('returns the first matching element by id', () => {
            expect(component.getFormElementById('findThisId').label).toBe('first');
        });
        it('handles when the id does not exist', () => {
            expect(component.getFormElementById('fakeId')).toBeUndefined();
        });
        it('handles when there is no form', () => {
            delete component.screen.form;
            expect(component.getFormElementById('findThisId')).toBeUndefined();
        });
    });

    describe('anyAddressFieldsPresent()', () => {
        it('is false when no address fields are present', () => {
            expect(component.anyAddressFieldsPresent()).toBeFalsy();
        });
        it('is true when only line1 is present', () => {
            expect(component.anyAddressFieldsPresent()).toBeFalsy();
            component.line1Field = { id: 'line1' };
            expect(component.anyAddressFieldsPresent()).toBeTruthy();
        });
        it('is true when only line2 is present', () => {
            expect(component.anyAddressFieldsPresent()).toBeFalsy();
            component.line2Field = { id: 'line2' };
            expect(component.anyAddressFieldsPresent()).toBeTruthy();
        });
        it('is true when only city is present', () => {
            expect(component.anyAddressFieldsPresent()).toBeFalsy();
            component.cityField = { id: 'city' };
            expect(component.anyAddressFieldsPresent()).toBeTruthy();
        });
        it('is true when only state is present', () => {
            expect(component.anyAddressFieldsPresent()).toBeFalsy();
            component.stateField = { id: 'state' };
            expect(component.anyAddressFieldsPresent()).toBeTruthy();
        });
        it('is true when only postalCode is present', () => {
            expect(component.anyAddressFieldsPresent()).toBeFalsy();
            component.postalCodeField = { id: 'postalCode' };
            expect(component.anyAddressFieldsPresent()).toBeTruthy();
        });
        it('is true when only country is present', () => {
            expect(component.anyAddressFieldsPresent()).toBeFalsy();
            component.countryField = { id: 'country' };
            expect(component.anyAddressFieldsPresent()).toBeTruthy();
        });
        it('is true when all fields are present', () => {
            expect(component.anyAddressFieldsPresent()).toBeFalsy();
            component.line1Field = { id: 'line1' };
            component.line2Field = { id: 'line2' };
            component.cityField = { id: 'city' };
            component.stateField = { id: 'state' };
            component.postalCodeField = { id: 'postalCode' };
            component.countryField = { id: 'country' };
            expect(component.anyAddressFieldsPresent()).toBeTruthy();
        });
    });

    describe('onFieldChanged(formElement: IFormElement)', () => {
        it('calls doAction, passing in the form with the valueChangedAction', () => {
            spyOn(component.actionService, 'doAction').and.callThrough();
            const action = 'changedAction';
            component.onFieldChanged({ id: 'onFieldChanged', valueChangedAction: { action } });
            expect(component.actionService.doAction).toHaveBeenCalledWith({ action }, mockForm);
        });
        it('does nothing when the provided element has no valueChangedAction', () => {
            spyOn(component.actionService, 'doAction').and.callThrough();
            component.onFieldChanged({ id: 'onFieldChanged' });
            expect(component.actionService.doAction).not.toHaveBeenCalled();
        });
    });

    describe('submitForm()', () => {
        it('calls buildFormPayload then does the submitButton action', () => {
            spyOn(formBuilder, 'buildFormPayload').and.returnValue(undefined);
            spyOn(component.actionService, 'doAction').and.callThrough();
            component.screen.submitButton = { action: 'submit' };
            component.submitForm();
            expect(formBuilder.buildFormPayload).toHaveBeenCalledWith(undefined, mockForm);
            expect(component.actionService.doAction).toHaveBeenCalledWith({ action: 'submit' }, mockForm);
        });
    });

    describe('secondaryButtonClicked()', () => {
        it('calls buildFormPayload then does the secondaryButton action', () => {
            spyOn(formBuilder, 'buildFormPayload').and.returnValue(undefined);
            spyOn(component.actionService, 'doAction').and.callThrough();
            component.screen.secondaryButton = { action: 'alternativeAction' };
            component.secondaryButtonClicked();
            expect(formBuilder.buildFormPayload).toHaveBeenCalledWith(undefined, mockForm);
            expect(component.actionService.doAction).toHaveBeenCalledWith({ action: 'alternativeAction' }, mockForm);
        });
    });

    describe('buildScreen()', () => {
        const firstNameField = { id: 'firstName', label: 'firstName' };
        const lastNameField = { id: 'lastName', label: 'lastName' };
        const form = JSON.parse(JSON.stringify(mockForm));
        form.formElements.push(firstNameField);
        form.formElements.push(lastNameField);
        beforeEach(() => {
            component.screen.form = form;
            spyOn(formBuilder, 'group').and.callThrough();
        });
        it('adds unhandled fields into extraElements and calls formBuilder.group when not a structured form', () => {
            component.screen.isStructuredForm = false;
            component.buildScreen();
            expect(component.extraElements.length).toBe(4);
            expect(component.extraElements[2].id).toBe('firstName');
            expect(component.extraElements[3].id).toBe('lastName');
            expect(formBuilder.group).toHaveBeenCalledWith(form);
        });
        it('a structured form removes handled fields from extraElements', () => {
            component.screen.isStructuredForm = true;
            component.buildScreen();
            expect(component.extraElements.length).toBe(2);
            expect(component.extraElements[0].id).toBe('findThisId');
            expect(component.extraElements[1].id).toBe('findThisId');
            expect(component.firstNameField).toEqual(firstNameField);
            expect(component.lastNameField).toEqual(lastNameField);
        });
    });
});
