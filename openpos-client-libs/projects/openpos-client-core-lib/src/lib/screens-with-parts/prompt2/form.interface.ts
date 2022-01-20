import { ActionItem } from '../../core/actions/action-item';
import { IAbstractScreen } from '../../core/interfaces/abstract-screen.interface';

export interface PromptPlusPlusUIMessage extends IAbstractScreen {
  items: BaseFormControl[];
  submitAction: ActionItem;

  /**
   * If available, the form will be populated with a scan result. If the form is
   * valid it will automatically be submitted.
   *
   * This is only allowed if there is a single input that accepts scan data.
   */
  submitOnScan?: boolean;

  /**
   * Action that is invoked instead of the `submitAction` if `submitOnScan` is
   * enabled. Using this method will provide the raw scan information via.
   * the action instead of validating with the form.
   */
  submitOnScanAction?: string;
}

// todo: other types - group?, toggle, select, etc.
export type DynamicFormControlType = 'text' | 'date';

export interface BaseFormControl {
  readonly id: string;
  readonly type: DynamicFormControlType;
}

export interface DynamicFormGroup extends BaseFormControl {
  readonly children: BaseFormControl[];
}

export interface BaseInputControl<T> extends BaseFormControl {
  readonly label: string;
  readonly hint?: string;
  readonly defaultValue?: T;
  readonly required: boolean;
}

export interface DynamicTextInput extends BaseInputControl<string> {
  readonly kind?: TextInputKind;
  readonly minimumLength?: number;
  readonly maximumLength?: number;
  readonly pattern?: string;
  readonly allowBarcodeScanEntry?: boolean;
  readonly placeholder?: string;
}

export enum TextInputKind {
  Text = 'Text',
  Number = 'Number',
  Password = 'Password'
}

export enum DateFormatKind {
  MMDD = 'MMDD',
  MMDDYY = 'MMDDYY',
  MMDDYYYY = 'MMDDYYYY',
  DDMMYY = 'DDMMYY',
  DDMMYYYY = 'DDMMYYYY'
}

export interface DynamicDateInput extends BaseInputControl<Date> {
  readonly format: DateFormatKind;
  readonly min?: Date;
  readonly max?: Date;
}

export interface DynamicToggleInput extends BaseInputControl<boolean> {
}
