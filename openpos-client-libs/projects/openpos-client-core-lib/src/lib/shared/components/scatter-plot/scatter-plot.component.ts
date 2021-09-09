import { Component, Input, OnInit } from '@angular/core';
import { LineGraphData, NgxChartSingleDataPoint, UIGraph, UIGraphMultiDataPoint } from '../data-tile/graph.interface';

@Component({
    selector: 'app-scatter-plot',
    templateUrl: './scatter-plot.component.html',
    styleUrls: ['./scatter-plot.component.scss']
})
export class ScatterPlotComponent implements OnInit {

    colorScheme = {
        domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA']
    };
    @Input()
    graph: UIGraph;
    @Input()
    width: number;
    @Input()
    height: number;

    data: LineGraphData[];
    showLegend = true;
    legendPosition: string;
    xAxisLabel: string;
    yAxisLabel: string;
    view: number[];

    constructor() { }

    ngOnInit() {
        if (this.graph) {
            this.showLegend = this.graph.legendEnabled;
            this.legendPosition = this.graph.legendPosition;
            this.xAxisLabel = this.graph.xaxisLabel;
            this.yAxisLabel = this.graph.yaxisLabel;
            this.data = this.convertDataToNGXFormat(this.graph.graphData);
        }
        console.log(`Initializing a line graph: data size ${this.data.length}, 
        xAxisLabel ${this.xAxisLabel}, yAxisLabel ${this.yAxisLabel}`);
    }

    screenDataUpdated() { }
    onSelect(event): void { }
    onActivate(event): void { }
    onDeactivate(event): void { }

    private convertDataToNGXFormat(graphData: UIGraphMultiDataPoint[]): LineGraphData[] {
        const convertedData: LineGraphData[] = [];
        for (const graphValue of graphData) {
            const series: any[] = [];
            if (graphValue.dataSeries) {
                for (const seriesValue of graphValue.dataSeries) {
                    series.push({
                        name: seriesValue.xvalue,
                        x: seriesValue.xvalue,
                        y: seriesValue.yvalue,
                        r: 10
                    });
                }
            }
            convertedData.push({
                name: graphValue.name,
                series
            });
        }
        return convertedData;
    }
}
