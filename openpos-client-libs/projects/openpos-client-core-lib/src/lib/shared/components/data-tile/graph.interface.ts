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

export interface UIGraphSingleDataPoint {
    xvalue;
    yvalue;
}

export interface UIGraphMultiDataPoint {
    name: string;
    dataSeries: UIGraphSingleDataPoint[];
}

export type LegendPosition = 'left' | 'right' | 'below';

export interface LineGraphData {
    name: string;
    series: LineDataPoint[];
}

export interface LineDataPoint {
    name: string | number;
    value: number;
}

export interface NgxChartSingleDataPoint {
    name: string;
    value: number;
}
