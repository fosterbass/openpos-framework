package org.jumpmind.pos.print;

import jpos.POSPrinterConst;
import org.jumpmind.pos.util.status.Status;

import java.util.Map;

abstract public class AbstractPOSPrinter implements IOpenposPrinter {

    public static final int STATUS_RECEIPT_PAPER_LOW = 0b00000001;
    public static final int STATUS_COVER_OPEN = 0b00000010;
    public static final int STATUS_RECEIPT_PAPER_OUT = 0b00000100;
    public static final int SLIP_LEADING_EDGE_SENSOR_COVERED = 0b00100000;

    IPrinterStatusReporter printerStatusReporter;

    Map<String, Object> settings;
    String printerName;
    PrinterCommands printerCommands = new PrinterCommandPlaceholders();

    @Override
    public String getPrinterName() {
        return this.printerName;
    }

    @Override
    public int getPrintWidth() {
        Object printWidthObject = settings.get("printWidth");
        Integer printWidth = null;
        if(printWidthObject != null) {
            if(printWidthObject instanceof String) {
                printWidth = Integer.parseInt((String) printWidthObject);
            } else {
                printWidth = (Integer) printWidthObject;
            }
        }
        if (printWidth == null) {
            printWidth = 48;
        }
        return printWidth;
    }

    @Override
    public String getCommand(String commandName) {
        return printerCommands.get(commandName);
    }

    @Override
    public void lineFeedAndCutPaper() {
        try {
            printNormal(POSPrinterConst.PTR_S_RECEIPT, printerCommands.get(PrinterCommands.CUT_FEED));  // epson will cut through barcode without some feed
            cutPaper(100);
        } catch (Exception e) {
            reportStatus("Failed to print line feed and cut the paper", e);
        }
    }

    @Override
    public int waitForDrawerClose(String cashDrawerId, long timeout) {
        long startTime = System.currentTimeMillis();
        int drawerState = DRAWER_OPEN;
        try {
            while (drawerState != DRAWER_CLOSED && System.currentTimeMillis() - startTime < timeout) {
                Thread.sleep(1000);
                drawerState = isDrawerOpen(cashDrawerId) ? DRAWER_OPEN : DRAWER_CLOSED;
            }
        } catch (Exception e) {
            String msg = String.format("Failure to read the status of the drawer: %s", cashDrawerId);
            reportStatus(msg, e);
        }
        return drawerState;
    }

    @Override
    public void printSlip(String text, int timeoutInMillis) {
        try {
            beginSlipMode();
            printNormal(POSPrinterConst.PTR_S_SLIP, text);
            endSlipMode();
        } catch (Exception ex) {
            reportStatus("Failed to print to slip station " + text, ex);
        }
    }

    void reportStatus(String message, Exception ex) {
        throw new PrintException(message, ex);
    }

    void refreshPrinterCommandsFromSettings() {
        this.printerCommands = new PrinterCommands();
        String printerCommandLocations = (String) settings.get("printerCommandLocations");
        String[] locationsSplit = printerCommandLocations.split(",");
        for (String printerCommandLocation : locationsSplit) {
            printerCommands.load(Thread.currentThread().getContextClassLoader().getResource(printerCommandLocation.trim()));
        }
    }

}
