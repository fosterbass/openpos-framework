import { Component, Input, OnInit } from '@angular/core';
import { NgxChartMultiDataPoint, NgxChartSingleDataPoint, UIGraph, UIGraphMultiDataPoint } from '../data-tile/graph.interface';

@Component({
  selector: 'app-line-graph',
  templateUrl: './line-graph.component.html',
  styleUrls: ['./line-graph.component.scss']
})
export class LineGraphComponent implements OnInit {

  colorScheme = {
    domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA']
  };
  @Input()
  graph: UIGraph;
  @Input()
  width: number;
  @Input()
  height: number;

  data: NgxChartMultiDataPoint[];

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

  private convertDataToNGXFormat(graphData: UIGraphMultiDataPoint[]): NgxChartMultiDataPoint[] {
    const convertedData: NgxChartMultiDataPoint[] = [];
    for (const graphValue of graphData) {
      const series: NgxChartSingleDataPoint[] = [];
      if (graphValue.dataSeries) {
        for (const seriesValue of graphValue.dataSeries) {
          series.push({
            name: seriesValue.xvalue,
            value: seriesValue.yvalue
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
