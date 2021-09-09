import { Component, Input, OnInit } from '@angular/core';
import { NgxChartSingleDataPoint, UIGraph, UIGraphSingleDataPoint } from '../data-tile/graph.interface';

@Component({
  selector: 'app-vertical-bar-graph',
  templateUrl: './vertical-bar-graph.component.html',
  styleUrls: ['./vertical-bar-graph.component.scss']
})
export class VerticalBarGraphComponent implements OnInit {
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

  constructor() { }

  ngOnInit() {
    if (this.graph) {
      this.data = this.convertDataToNGXFormat(this.graph.graphData);
    }
  }

  screenDataUpdated() {
  }
  onSelect(event): void { }

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
