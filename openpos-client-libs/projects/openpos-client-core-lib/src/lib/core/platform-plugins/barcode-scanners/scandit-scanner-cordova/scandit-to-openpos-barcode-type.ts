import { ScanDataType } from '../scanner';
import { Symbology } from './types/barcode';

export class ScanditBarcodeUtils {

    private static scanditToOpenPosMap = new Map<Symbology, ScanDataType>([
        [Symbology.Aztec, 'AZTEK'],
        [Symbology.Codabar, 'CODABAR'],
        [Symbology.Code11, 'CODE11'],
        [Symbology.Code25, 'CODE25_NI2OF5'],
        [Symbology.Code39, 'CODE39'],
        [Symbology.Code93, 'CODE93'],
        [Symbology.Code128, 'CODE128'],
        [Symbology.EAN8, 'EAN8'],
        [Symbology.GS1Databar, 'GS1DATABAR'],
        [Symbology.MaxiCode, 'MAXICODE'],
        [Symbology.MSIPlessey, 'MSI_PLESSEY'],
        [Symbology.PDF417, 'PDF417'],
        [Symbology.QR, 'QRCODE'],
        [Symbology.RM4SCC, 'RM4SCC'],
        [Symbology.EAN13UPCA, 'UPCA'],
        [Symbology.UPCE, 'UPCE'],
        [Symbology.InterleavedTwoOfFive, 'ITF'],
        [Symbology.ITF, 'ITF'],
    ]);


    static convertToOpenposType(type: Symbology, data: string): ScanDataType {
        if (type === Symbology.EAN13UPCA && data[0] !== '0') {
            return 'EAN13';
        }

        if (this.scanditToOpenPosMap.has(type)) {
            return this.scanditToOpenPosMap.get(type);
        }

        throw Error(`Barcode type ${type} not supported`);
    }

    static convertFromOpenposType(type: ScanDataType): Symbology {
        const matches = Array.from(this.scanditToOpenPosMap.entries()).filter(value => value[1] === type);

        if (matches.length > 0) {
            return matches[0][0];
        }

        throw Error(`Barcode type ${type} not supported`);
    }
}
