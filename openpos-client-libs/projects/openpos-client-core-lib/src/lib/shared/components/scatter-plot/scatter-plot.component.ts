import { Component, Input, OnInit } from '@angular/core';
import { UIScatterPlot, UIGraphMultiDataPoint, NgxBubbleChartMultiDataPoint } from '../data-tile/graph.interface';

@Component({
    selector: 'app-scatter-plot',
    templateUrl: './scatter-plot.component.html',
    styleUrls: ['./scatter-plot.component.scss']
})
export class ScatterPlotComponent implements OnInit {

    colorScheme = {
        domain: ['#34a4eb', '#5AA454', '#A10A28', '#C7B42C', '#AAAAAA', '#10537D', '#941589', '#8327C4']
    };
    @Input()
    graph: UIScatterPlot;
    @Input()
    width: number;
    @Input()
    height: number;

    data: NgxBubbleChartMultiDataPoint[];

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

    private convertDataToNGXFormat(graphData: UIGraphMultiDataPoint[]): NgxBubbleChartMultiDataPoint[] {
        const convertedData: NgxBubbleChartMultiDataPoint[] = [];
        for (const graphValue of graphData) {
            const series = [];
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
