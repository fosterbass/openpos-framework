import { Component, Input } from '@angular/core';
import { DataTileSize, DataTileSizes, UIDataTile } from './data-tile.interface';

@Component({
    selector: 'app-data-tile',
    templateUrl: './data-tile.component.html',
    styleUrls: ['./data-tile.component.scss']

})
export class DataTileComponent {
    @Input()
    tile: UIDataTile;

    getSizeStyles(size: DataTileSize): string {
        return `width: ${this.getSizeWidthPercent(size)}; height: ${this.getSizeHeight(size)}px;`;
    }

    getSizeWidthPercent(size: DataTileSize): string {
        switch (size) {
            case DataTileSizes.SMALL:
                return '25%';
            case DataTileSizes.MEDIUM:
                return '33%';
            case DataTileSizes.LARGE:
                return '50%';
            case DataTileSizes.EXTRA_LARGE:
                return '75%';
            default:
                return '33%';
        }
    }

    getSizeWidthPixels(size: DataTileSize): number {
        const sizePercent = parseInt(this.getSizeWidthPercent(size), 10) / 100;
        return window.innerWidth * sizePercent;
    }

    getSizeHeight(size: DataTileSize): number {
        return 400;
    }
}
