package org.jumpmind.pos.print;

import jpos.CashDrawer;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.services.EventCallbacks;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jumpmind.pos.util.BoolUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JavaPOSPrinter extends AbstractPOSPrinter implements IOpenposPrinter {

    @Delegate
    POSPrinter posPrinter = new POSPrinter();

    CashDrawer cashDrawer = new CashDrawer();

    Map<String, File> images = new HashMap<>();

    @Override
    public void init(Map<String, Object> settings, IPrinterStatusReporter printerStatusReporter) {
        try {
            this.printerStatusReporter = printerStatusReporter;
            this.settings = settings;
            this.refreshPrinterCommandsFromSettings();
        } catch (Exception ex) {
            reportStatus("Failed to init device(s)", ex);
        }
    }


    @Override
    public void open(String logicalName, EventCallbacks cb) throws JposException {
        try {
            this.printerName = logicalName;
            open((String) settings.get("printerName"));
            claim(100);
            setDeviceEnabled(true);
            if (BoolUtils.toBoolean(settings.get("cashDrawerEnabled"))) {
                cashDrawer.open((String) settings.get("cashDrawerName"));
                cashDrawer.claim(100);
                cashDrawer.setDeviceEnabled(true);
            }
        } catch (Exception ex) {
            reportStatus("Failed to open device(s)", ex);
        }
    }

    @Override
    public void printImage(String name, InputStream imageIS) {
        try {
            String imageFileDir = (String) settings.get("imageFileDir");
            String imageFileName = String.format("%s/%s", imageFileDir, name);
            File file = this.images.get(imageFileName);
            if (file == null || !file.exists()) {
                file = new File(imageFileDir, name);
                FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(imageIS));
                this.images.put(imageFileName, file);
            }
            // this could probably be sped up by using memory mapped bitmaps
            this.posPrinter.printBitmap(POSPrinterConst.PTR_S_RECEIPT, imageFileName,
                    POSPrinterConst.PTR_BM_ASIS, POSPrinterConst.PTR_BM_CENTER);
        } catch (Exception ex) {
            reportStatus("Failed to print image: " + name, ex);
        }
    }

    @Override
    public void openCashDrawer(String cashDrawerId) {
        try {
            cashDrawer.openDrawer();
        } catch (Exception ex) {
            reportStatus("Failed to open the cash drawer", ex);
        }
    }

    @Override
    public int readPrinterStatus() {
        int status = 0;
        try {
            if (this.getRecEmpty()) {
                status &= STATUS_RECEIPT_PAPER_OUT;
            }
            if (this.getRecNearEnd()) {
                status &= STATUS_RECEIPT_PAPER_LOW;
            }
            if (this.getCoverOpen()) {
                status &= STATUS_COVER_OPEN;
            }
        } catch (Exception ex) {
            reportStatus("Failed to open the cash drawer", ex);
        }
        return status;
    }

    @Override
    public boolean isDrawerOpen(String cashDrawerId) {
        try {
            boolean cashDrawerOpened = cashDrawer.getDrawerOpened();
            String message = cashDrawerOpened ? "Cash drawer is opened" : "Cash drawer is not opened";
            log.info(message);

            return cashDrawerOpened;
        } catch (JposException ex) {
            throw new PrintException("Could not detect if the cash drawer is open", ex);
        }
    }

    @Override
    public int waitForDrawerClose(String cashDrawerId, long timeout) {
        try {
            this.cashDrawer.waitForDrawerClose(20000, 44000, 2000, 1000);
        } catch (Exception e) {
            String msg = String.format("Failure to read the status of the drawer: %s", cashDrawerId);
            reportStatus(msg, e);
        }
        return DRAWER_CLOSED;
    }

    @Override
    public void beginSlipMode() {
        try {
            beginInsertion(60);
            endInsertion();
        } catch (JposException ex) {
            throw new PrintException("Failed to beginSlipMode", ex);
        }
    }

    @Override
    public void endSlipMode() {
        try {
            beginRemoval(60);
            endRemoval();
        } catch (JposException ex) {
            throw new PrintException("Failed to beginSlipMode", ex);
        }
    }

    @Override
    public String readMicr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteInstance() throws JposException {

    }
}
