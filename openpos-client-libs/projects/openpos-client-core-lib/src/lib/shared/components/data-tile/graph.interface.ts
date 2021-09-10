export interface UIGraph {
    graphType: string;
    title: string;
    xaxisLabel: string;
    yaxisLabel: string;
    legendEnabled: boolean;
    legendTitle: string;
    legendPosition: LegendPosition;
    graphData: any[];
}

export interface UILineGraph extends UIGraph {
    graphType: 'Line';
    graphData: UIGraphMultiDataPoint[];
}

export interface UIBarGraph extends UIGraph {
    graphType: 'Bar';
    graphData: UIGraphSingleDataPoint[];
}

export interface UIPieChart extends UIGraph {
    graphType: 'PieChart';
    graphData: UIGraphSingleDataPoint[];
}

export interface UIScatterPlot extends UIGraph {
    graphType: 'ScatterPlot';
    graphData: UIGraphMultiDataPoint[];
}

export interface UIGraphSingleDataPoint {
    xvalue: number | string;
    yvalue: number;
}

export interface UIGraphMultiDataPoint {
    name: string;
    dataSeries: UIGraphSingleDataPoint[];
}

export type LegendPosition = 'left' | 'right' | 'below';

export interface NgxChartMultiDataPoint {
    name: string;
    series: NgxChartSingleDataPoint[];
}

export interface NgxChartSingleDataPoint {
    name: number | string;
    value: number;
}

export interface NgxBubbleChartMultiDataPoint {
    name: string;
    series: NgxBubbleChartSingleDataPoint[];
}

export interface NgxBubbleChartSingleDataPoint {
    name: string;
    x: number | string;
    y: number;
    r: number;
}
