import { Component, Input, OnInit } from '@angular/core';
import { LineGraphData, UIGraph, UIGraphMultiDataPoint } from '../data-tile/graph.interface';

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

    constructor() { }

    ngOnInit() {
        if (this.graph) {
            this.data = this.convertDataToNGXFormat(this.graph.graphData);
        }
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
