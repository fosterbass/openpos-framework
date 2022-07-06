import { IAbstractScreen } from '../../core/interfaces/abstract-screen.interface';
import { IActionItem } from '../../core/actions/action-item.interface';
import { IDataTableRow } from '../../shared/components/grid-table/data-table-row.interface';

export interface DataTableInterface extends IAbstractScreen {
    instructions: string;
    columnHeaders: string[];
    rows: IDataTableRow[];
    actionButtons: IActionItem[];
}
