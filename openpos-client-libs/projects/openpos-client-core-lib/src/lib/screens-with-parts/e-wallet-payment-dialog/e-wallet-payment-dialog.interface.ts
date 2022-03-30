import { IActionItem } from '../../core/actions/action-item.interface';
import { IAbstractScreen } from '../../core/interfaces/abstract-screen.interface';
import { ScanInterface } from '../../shared/screen-parts/scan-part/scan-part.interface';

export interface EWalletPaymentDialogInterface extends IAbstractScreen{
    qrCodeUrl: string;
    message: string;
    cancelButton: IActionItem;
    scan: ScanInterface;
}
