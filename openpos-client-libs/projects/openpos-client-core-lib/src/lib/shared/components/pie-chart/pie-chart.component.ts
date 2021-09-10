import { Component, Input, OnInit } from '@angular/core';
import { NgxChartSingleDataPoint, UIPieChart, UIGraphSingleDataPoint } from '../data-tile/graph.interface';

@Component({
    selector: 'app-pie-chart',
    templateUrl: './pie-chart.component.html',
    styleUrls: ['./pie-chart.component.scss']
})
export class PieChartComponent implements OnInit {
    colorScheme = {
        domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA']
    };
    @Input()
    graph: UIPieChart;
    @Input()
    width: number;
    @Input()
    height: number;

    data: NgxChartSingleDataPoint[];

    constructor() { }

    ngOnInit() {
        if (this.graph) {
            this.data = this.convertDataToNGXFormat(this.graph.graphData);
        }
    }

    screenDataUpdated() {
    }
    onSelect(event): void { }
    onActivate(event): void { }
    onDeactivate(event): void { }

    private convertDataToNGXFormat(graphData: UIGraphSingleDataPoint[]): NgxChartSingleDataPoint[] {
        const convertedData = [];
        for (const graphValue of graphData) {
            convertedData.push({
                name: graphValue.xvalue,
                value: graphValue.yvalue
            });
        }
        return convertedData;
    }
}