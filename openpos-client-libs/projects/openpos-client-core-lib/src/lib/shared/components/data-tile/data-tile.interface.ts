import { UIGraph } from './graph.interface';

export interface UIDataTile {
    tileId: string;
    title: string;
    description: string;
    size: DataTileSize;
    graph: UIGraph;
    table;
}

export class DataTileSizes {
    static SMALL = 'Small';
    static MEDIUM = 'Medium';
    static LARGE = 'Large';
    static EXTRA_LARGE = 'ExtraLarge';
}

export type DataTileSize = 'Small' | 'Medium' | 'Large' | 'ExtraLarge';
