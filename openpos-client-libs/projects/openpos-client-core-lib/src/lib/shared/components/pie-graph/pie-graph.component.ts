import { Component, Input, OnInit } from '@angular/core';
import { NgxChartSingleDataPoint, UIGraph, UIGraphSingleDataPoint } from '../data-tile/graph.interface';

@Component({
    selector: 'app-pie-graph',
    templateUrl: './pie-graph.component.html',
    styleUrls: ['./pie-graph.component.scss']
})
export class PieGraphComponent implements OnInit {
    colorScheme = {
        domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA']
    };
    @Input()
    graph: UIGraph;
    @Input()
    width: number;
    @Input()
    height: number;

    data: NgxChartSingleDataPoint[];
    showLegend = true;
    legendPosition: string;
    xAxisLabel: string;
    yAxisLabel: string;

    constructor() { }

    ngOnInit() {
        if (this.graph) {
            this.showLegend = this.graph.legendEnabled;
            this.legendPosition = this.graph.legendPosition;
            this.xAxisLabel = this.graph.xaxisLabel;
            this.yAxisLabel = this.graph.yaxisLabel;
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
