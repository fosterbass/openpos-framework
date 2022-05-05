import { Component, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core';
import { ITab } from './tab.interface';

@Component({
    selector: 'app-tabbed-content-card',
    templateUrl: './tabbed-content-card.component.html',
    styleUrls: ['./tabbed-content-card.component.scss']
})
export class TabbedContentCardComponent implements OnInit {

    @Input() tabs: ITab[];

    @Input() selectedTabTemplate: TemplateRef<any>;

    @Input() showIcons = false;

    @Output() tabChange = new EventEmitter<string>();

    selectedTabId: string;

    ngOnInit(): void {
        if (this.tabs?.length > 0) {
            this.selectedTabId = this.tabs[0].tabId;
            this.tabChange.emit(this.selectedTabId);
        }
    }

    isSelected(tabId: string): boolean {
        return this.selectedTabId === tabId;
    }

    tabChanged(tabId: string): void {
        this.selectedTabId = tabId;
        this.tabChange.emit(tabId);
    }
}
