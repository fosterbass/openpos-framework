package org.jumpmind.pos.print;

import jpos.CashDrawer;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.events.DirectIOListener;
import jpos.events.ErrorListener;
import jpos.events.OutputCompleteListener;
import jpos.events.StatusUpdateListener;
import jpos.services.EventCallbacks;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jumpmind.pos.util.AppUtils;
import org.jumpmind.pos.util.BoolUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JavaPOSPrinter extends AbstractPOSPrinter implements IOpenposPrinter {

    POSPrinter posPrinter = new POSPrinter();

    CashDrawer cashDrawer = new CashDrawer();

    Map<String, File> images = new HashMap<>();

    @Override
    public void init(Map<String, Object> settings) {
        try {
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
    public void close() throws JposException {
        this.posPrinter.close();
        if (BoolUtils.toBoolean(settings.get("cashDrawerEnabled"))) {
            this.cashDrawer.close();
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
            /* Most of the time when this method is called we are expecting the cash drawer to be open.
             * Some drivers (namely hp engage one epson printer), when opened, takes a while to update their
             * internal status.  Wait for up to a configurable amount of time for the drawer to report
             * as open. */
            int waitTimeInMs = Integer.parseInt(settings.getOrDefault("waitForOpenTimeInMs", "2000").toString());
            for (int i = 0; i < waitTimeInMs/50 && !cashDrawer.getDrawerOpened(); i++) {
                AppUtils.sleep(50);
            }
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
            if (isDrawerOpen(cashDrawerId)) {
                this.cashDrawer.waitForDrawerClose(20000, 44000, 2000, 1000);
            }
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

    public int getCapCharacterSet() throws JposException {
        return this.posPrinter.getCapCharacterSet();
    }

    public boolean getCapConcurrentJrnRec() throws JposException {
        return this.posPrinter.getCapConcurrentJrnRec();
    }

    public boolean getCapConcurrentJrnSlp() throws JposException {
        return this.posPrinter.getCapConcurrentJrnSlp();
    }

    public boolean getCapConcurrentRecSlp() throws JposException {
        return this.posPrinter.getCapConcurrentRecSlp();
    }

    public boolean getCapCoverSensor() throws JposException {
        return this.posPrinter.getCapCoverSensor();
    }

    public boolean getCapJrn2Color() throws JposException {
        return this.posPrinter.getCapJrn2Color();
    }

    public boolean getCapJrnBold() throws JposException {
        return this.posPrinter.getCapJrnBold();
    }

    public boolean getCapJrnDhigh() throws JposException {
        return this.posPrinter.getCapJrnDhigh();
    }

    public boolean getCapJrnDwide() throws JposException {
        return this.posPrinter.getCapJrnDwide();
    }

    public boolean getCapJrnDwideDhigh() throws JposException {
        return this.posPrinter.getCapJrnDwideDhigh();
    }

    public boolean getCapJrnEmptySensor() throws JposException {
        return this.posPrinter.getCapJrnEmptySensor();
    }

    public boolean getCapJrnItalic() throws JposException {
        return this.posPrinter.getCapJrnItalic();
    }

    public boolean getCapJrnNearEndSensor() throws JposException {
        return this.posPrinter.getCapJrnNearEndSensor();
    }

    public boolean getCapJrnPresent() throws JposException {
        return this.posPrinter.getCapJrnPresent();
    }

    public boolean getCapJrnUnderline() throws JposException {
        return this.posPrinter.getCapJrnUnderline();
    }

    public boolean getCapRec2Color() throws JposException {
        return this.posPrinter.getCapRec2Color();
    }

    public boolean getCapRecBarCode() throws JposException {
        return this.posPrinter.getCapRecBarCode();
    }

    public boolean getCapRecBitmap() throws JposException {
        return this.posPrinter.getCapRecBitmap();
    }

    public boolean getCapRecBold() throws JposException {
        return this.posPrinter.getCapRecBold();
    }

    public boolean getCapRecDhigh() throws JposException {
        return this.posPrinter.getCapRecDhigh();
    }

    public boolean getCapRecDwide() throws JposException {
        return this.posPrinter.getCapRecDwide();
    }

    public boolean getCapRecDwideDhigh() throws JposException {
        return this.posPrinter.getCapRecDwideDhigh();
    }

    public boolean getCapRecEmptySensor() throws JposException {
        return this.posPrinter.getCapRecEmptySensor();
    }

    public boolean getCapRecItalic() throws JposException {
        return this.posPrinter.getCapRecItalic();
    }

    public boolean getCapRecLeft90() throws JposException {
        return this.posPrinter.getCapRecLeft90();
    }

    public boolean getCapRecNearEndSensor() throws JposException {
        return this.posPrinter.getCapRecNearEndSensor();
    }

    public boolean getCapRecPapercut() throws JposException {
        return this.posPrinter.getCapRecPapercut();
    }

    public boolean getCapRecPresent() throws JposException {
        return this.posPrinter.getCapRecPresent();
    }

    public boolean getCapRecRight90() throws JposException {
        return this.posPrinter.getCapRecRight90();
    }

    public boolean getCapRecRotate180() throws JposException {
        return this.posPrinter.getCapRecRotate180();
    }

    public boolean getCapRecStamp() throws JposException {
        return this.posPrinter.getCapRecStamp();
    }

    public boolean getCapRecUnderline() throws JposException {
        return this.posPrinter.getCapRecUnderline();
    }

    public boolean getCapSlp2Color() throws JposException {
        return this.posPrinter.getCapSlp2Color();
    }

    public boolean getCapSlpBarCode() throws JposException {
        return this.posPrinter.getCapSlpBarCode();
    }

    public boolean getCapSlpBitmap() throws JposException {
        return this.posPrinter.getCapSlpBitmap();
    }

    public boolean getCapSlpBold() throws JposException {
        return this.posPrinter.getCapSlpBold();
    }

    public boolean getCapSlpDhigh() throws JposException {
        return this.posPrinter.getCapSlpDhigh();
    }

    public boolean getCapSlpDwide() throws JposException {
        return this.posPrinter.getCapSlpDwide();
    }

    public boolean getCapSlpDwideDhigh() throws JposException {
        return this.posPrinter.getCapSlpDwideDhigh();
    }

    public boolean getCapSlpEmptySensor() throws JposException {
        return this.posPrinter.getCapSlpEmptySensor();
    }

    public boolean getCapSlpFullslip() throws JposException {
        return this.posPrinter.getCapSlpFullslip();
    }

    public boolean getCapSlpItalic() throws JposException {
        return this.posPrinter.getCapSlpItalic();
    }

    public boolean getCapSlpLeft90() throws JposException {
        return this.posPrinter.getCapSlpLeft90();
    }

    public boolean getCapSlpNearEndSensor() throws JposException {
        return this.posPrinter.getCapSlpNearEndSensor();
    }

    public boolean getCapSlpPresent() throws JposException {
        return this.posPrinter.getCapSlpPresent();
    }

    public boolean getCapSlpRight90() throws JposException {
        return this.posPrinter.getCapSlpRight90();
    }

    public boolean getCapSlpRotate180() throws JposException {
        return this.posPrinter.getCapSlpRotate180();
    }

    public boolean getCapSlpUnderline() throws JposException {
        return this.posPrinter.getCapSlpUnderline();
    }

    public boolean getCapTransaction() throws JposException {
        return this.posPrinter.getCapTransaction();
    }

    public int getCapPowerReporting() throws JposException {
        return this.posPrinter.getCapPowerReporting();
    }

    public int getCapJrnCartridgeSensor() throws JposException {
        return this.posPrinter.getCapJrnCartridgeSensor();
    }

    public int getCapJrnColor() throws JposException {
        return this.posPrinter.getCapJrnColor();
    }

    public int getCapRecCartridgeSensor() throws JposException {
        return this.posPrinter.getCapRecCartridgeSensor();
    }

    public int getCapRecColor() throws JposException {
        return this.posPrinter.getCapRecColor();
    }

    public int getCapRecMarkFeed() throws JposException {
        return this.posPrinter.getCapRecMarkFeed();
    }

    public boolean getCapSlpBothSidesPrint() throws JposException {
        return this.posPrinter.getCapSlpBothSidesPrint();
    }

    public int getCapSlpCartridgeSensor() throws JposException {
        return this.posPrinter.getCapSlpCartridgeSensor();
    }

    public int getCapSlpColor() throws JposException {
        return this.posPrinter.getCapSlpColor();
    }

    public boolean getCapMapCharacterSet() throws JposException {
        return this.posPrinter.getCapMapCharacterSet();
    }

    public boolean getCapStatisticsReporting() throws JposException {
        return this.posPrinter.getCapStatisticsReporting();
    }

    public boolean getCapUpdateStatistics() throws JposException {
        return this.posPrinter.getCapUpdateStatistics();
    }

    public boolean getCapCompareFirmwareVersion() throws JposException {
        return this.posPrinter.getCapCompareFirmwareVersion();
    }

    public boolean getCapConcurrentPageMode() throws JposException {
        return this.posPrinter.getCapConcurrentPageMode();
    }

    public boolean getCapRecPageMode() throws JposException {
        return this.posPrinter.getCapRecPageMode();
    }

    public boolean getCapSlpPageMode() throws JposException {
        return this.posPrinter.getCapSlpPageMode();
    }

    public boolean getCapUpdateFirmware() throws JposException {
        return this.posPrinter.getCapUpdateFirmware();
    }

    public int getCapRecRuledLine() throws JposException {
        return this.posPrinter.getCapRecRuledLine();
    }

    public int getCapSlpRuledLine() throws JposException {
        return this.posPrinter.getCapSlpRuledLine();
    }

    public boolean getAsyncMode() throws JposException {
        return this.posPrinter.getAsyncMode();
    }

    public void setAsyncMode(boolean asyncMode) throws JposException {
        this.posPrinter.setAsyncMode(asyncMode);
    }

    public int getCharacterSet() throws JposException {
        return this.posPrinter.getCharacterSet();
    }

    public void setCharacterSet(int characterSet) throws JposException {
        this.posPrinter.setCharacterSet(characterSet);
    }

    public String getCharacterSetList() throws JposException {
        return this.posPrinter.getCharacterSetList();
    }

    public boolean getCoverOpen() throws JposException {
        return this.posPrinter.getCoverOpen();
    }

    public int getErrorLevel() throws JposException {
        return this.posPrinter.getErrorLevel();
    }

    public int getErrorStation() throws JposException {
        return this.posPrinter.getErrorStation();
    }

    public String getErrorString() throws JposException {
        return this.posPrinter.getErrorString();
    }

    public boolean getFlagWhenIdle() throws JposException {
        return this.posPrinter.getFlagWhenIdle();
    }

    public void setFlagWhenIdle(boolean flagWhenIdle) throws JposException {
        this.posPrinter.setFlagWhenIdle(flagWhenIdle);
    }

    public String getFontTypefaceList() throws JposException {
        return this.posPrinter.getFontTypefaceList();
    }

    public boolean getJrnEmpty() throws JposException {
        return this.posPrinter.getJrnEmpty();
    }

    public boolean getJrnLetterQuality() throws JposException {
        return this.posPrinter.getJrnLetterQuality();
    }

    public void setJrnLetterQuality(boolean jrnLetterQuality) throws JposException {
        this.posPrinter.setJrnLetterQuality(jrnLetterQuality);
    }

    public int getJrnLineChars() throws JposException {
        return this.posPrinter.getJrnLineChars();
    }

    public void setJrnLineChars(int jrnLineChars) throws JposException {
        this.posPrinter.setJrnLineChars(jrnLineChars);
    }

    public String getJrnLineCharsList() throws JposException {
        return this.posPrinter.getJrnLineCharsList();
    }

    public int getJrnLineHeight() throws JposException {
        return this.posPrinter.getJrnLineHeight();
    }

    public void setJrnLineHeight(int jrnLineHeight) throws JposException {
        this.posPrinter.setJrnLineHeight(jrnLineHeight);
    }

    public int getJrnLineSpacing() throws JposException {
        return this.posPrinter.getJrnLineSpacing();
    }

    public void setJrnLineSpacing(int jrnLineSpacing) throws JposException {
        this.posPrinter.setJrnLineSpacing(jrnLineSpacing);
    }

    public int getJrnLineWidth() throws JposException {
        return this.posPrinter.getJrnLineWidth();
    }

    public boolean getJrnNearEnd() throws JposException {
        return this.posPrinter.getJrnNearEnd();
    }

    public int getMapMode() throws JposException {
        return this.posPrinter.getMapMode();
    }

    public void setMapMode(int mapMode) throws JposException {
        this.posPrinter.setMapMode(mapMode);
    }

    public int getOutputID() throws JposException {
        return this.posPrinter.getOutputID();
    }

    public String getRecBarCodeRotationList() throws JposException {
        return this.posPrinter.getRecBarCodeRotationList();
    }

    public boolean getRecEmpty() throws JposException {
        return this.posPrinter.getRecEmpty();
    }

    public boolean getRecLetterQuality() throws JposException {
        return this.posPrinter.getRecLetterQuality();
    }

    public void setRecLetterQuality(boolean recLetterQuality) throws JposException {
        this.posPrinter.setRecLetterQuality(recLetterQuality);
    }

    public int getRecLineChars() throws JposException {
        return this.posPrinter.getRecLineChars();
    }

    public void setRecLineChars(int recLineChars) throws JposException {
        this.posPrinter.setRecLineChars(recLineChars);
    }

    public String getRecLineCharsList() throws JposException {
        return this.posPrinter.getRecLineCharsList();
    }

    public int getRecLineHeight() throws JposException {
        return this.posPrinter.getRecLineHeight();
    }

    public void setRecLineHeight(int recLineHeight) throws JposException {
        this.posPrinter.setRecLineHeight(recLineHeight);
    }

    public int getRecLineSpacing() throws JposException {
        return this.posPrinter.getRecLineSpacing();
    }

    public void setRecLineSpacing(int recLineSpacing) throws JposException {
        this.posPrinter.setRecLineSpacing(recLineSpacing);
    }

    public int getRecLinesToPaperCut() throws JposException {
        return this.posPrinter.getRecLinesToPaperCut();
    }

    public int getRecLineWidth() throws JposException {
        return this.posPrinter.getRecLineWidth();
    }

    public boolean getRecNearEnd() throws JposException {
        return this.posPrinter.getRecNearEnd();
    }

    public int getRecSidewaysMaxChars() throws JposException {
        return this.posPrinter.getRecSidewaysMaxChars();
    }

    public int getRecSidewaysMaxLines() throws JposException {
        return this.posPrinter.getRecSidewaysMaxLines();
    }

    public int getRotateSpecial() throws JposException {
        return this.posPrinter.getRotateSpecial();
    }

    public void setRotateSpecial(int rotateSpecial) throws JposException {
        this.posPrinter.setRotateSpecial(rotateSpecial);
    }

    public String getSlpBarCodeRotationList() throws JposException {
        return this.posPrinter.getSlpBarCodeRotationList();
    }

    public boolean getSlpEmpty() throws JposException {
        return this.posPrinter.getSlpEmpty();
    }

    public boolean getSlpLetterQuality() throws JposException {
        return this.posPrinter.getSlpLetterQuality();
    }

    public void setSlpLetterQuality(boolean recLetterQuality) throws JposException {
        this.posPrinter.setSlpLetterQuality(recLetterQuality);
    }

    public int getSlpLineChars() throws JposException {
        return this.posPrinter.getSlpLineChars();
    }

    public void setSlpLineChars(int recLineChars) throws JposException {
        this.posPrinter.setSlpLineChars(recLineChars);
    }

    public String getSlpLineCharsList() throws JposException {
        return this.posPrinter.getSlpLineCharsList();
    }

    public int getSlpLineHeight() throws JposException {
        return this.posPrinter.getSlpLineHeight();
    }

    public void setSlpLineHeight(int recLineHeight) throws JposException {
        this.posPrinter.setSlpLineHeight(recLineHeight);
    }

    public int getSlpLinesNearEndToEnd() throws JposException {
        return this.posPrinter.getSlpLinesNearEndToEnd();
    }

    public int getSlpLineSpacing() throws JposException {
        return this.posPrinter.getSlpLineSpacing();
    }

    public void setSlpLineSpacing(int recLineSpacing) throws JposException {
        this.posPrinter.setSlpLineSpacing(recLineSpacing);
    }

    public int getSlpLineWidth() throws JposException {
        return this.posPrinter.getSlpLineWidth();
    }

    public int getSlpMaxLines() throws JposException {
        return this.posPrinter.getSlpMaxLines();
    }

    public boolean getSlpNearEnd() throws JposException {
        return this.posPrinter.getSlpNearEnd();
    }

    public int getSlpSidewaysMaxChars() throws JposException {
        return this.posPrinter.getSlpSidewaysMaxChars();
    }

    public int getSlpSidewaysMaxLines() throws JposException {
        return this.posPrinter.getSlpSidewaysMaxLines();
    }

    public int getPowerNotify() throws JposException {
        return this.posPrinter.getPowerNotify();
    }

    public void setPowerNotify(int powerNotify) throws JposException {
        this.posPrinter.setPowerNotify(powerNotify);
    }

    public int getPowerState() throws JposException {
        return this.posPrinter.getPowerState();
    }

    public int getCartridgeNotify() throws JposException {
        return this.posPrinter.getCartridgeNotify();
    }

    public void setCartridgeNotify(int notify) throws JposException {
        this.posPrinter.setCartridgeNotify(notify);
    }

    public int getJrnCartridgeState() throws JposException {
        return this.posPrinter.getJrnCartridgeState();
    }

    public int getJrnCurrentCartridge() throws JposException {
        return this.posPrinter.getJrnCurrentCartridge();
    }

    public void setJrnCurrentCartridge(int cartridge) throws JposException {
        this.posPrinter.setJrnCurrentCartridge(cartridge);
    }

    public int getRecCartridgeState() throws JposException {
        return this.posPrinter.getRecCartridgeState();
    }

    public int getRecCurrentCartridge() throws JposException {
        return this.posPrinter.getRecCurrentCartridge();
    }

    public void setRecCurrentCartridge(int cartridge) throws JposException {
        this.posPrinter.setRecCurrentCartridge(cartridge);
    }

    public int getSlpCartridgeState() throws JposException {
        return this.posPrinter.getSlpCartridgeState();
    }

    public int getSlpCurrentCartridge() throws JposException {
        return this.posPrinter.getSlpCurrentCartridge();
    }

    public void setSlpCurrentCartridge(int cartridge) throws JposException {
        this.posPrinter.setSlpCurrentCartridge(cartridge);
    }

    public int getSlpPrintSide() throws JposException {
        return this.posPrinter.getSlpPrintSide();
    }

    public boolean getMapCharacterSet() throws JposException {
        return this.posPrinter.getMapCharacterSet();
    }

    public void setMapCharacterSet(boolean mapCharacterSet) throws JposException {
        this.posPrinter.setMapCharacterSet(mapCharacterSet);
    }

    public String getRecBitmapRotationList() throws JposException {
        return this.posPrinter.getRecBitmapRotationList();
    }

    public String getSlpBitmapRotationList() throws JposException {
        return this.posPrinter.getSlpBitmapRotationList();
    }

    public String getPageModeArea() throws JposException {
        return this.posPrinter.getPageModeArea();
    }

    public int getPageModeDescriptor() throws JposException {
        return this.posPrinter.getPageModeDescriptor();
    }

    public int getPageModeHorizontalPosition() throws JposException {
        return this.posPrinter.getPageModeHorizontalPosition();
    }

    public void setPageModeHorizontalPosition(int position) throws JposException {
        this.posPrinter.setPageModeHorizontalPosition(position);
    }

    public String getPageModePrintArea() throws JposException {
        return this.posPrinter.getPageModePrintArea();
    }

    public void setPageModePrintArea(String area) throws JposException {
        this.posPrinter.setPageModePrintArea(area);
    }

    public int getPageModePrintDirection() throws JposException {
        return this.posPrinter.getPageModePrintDirection();
    }

    public void setPageModePrintDirection(int direction) throws JposException {
        this.posPrinter.setPageModePrintDirection(direction);
    }

    public int getPageModeStation() throws JposException {
        return this.posPrinter.getPageModeStation();
    }

    public void setPageModeStation(int station) throws JposException {
        this.posPrinter.setPageModeStation(station);
    }

    public int getPageModeVerticalPosition() throws JposException {
        return this.posPrinter.getPageModeVerticalPosition();
    }

    public void setPageModeVerticalPosition(int position) throws JposException {
        this.posPrinter.setPageModeVerticalPosition(position);
    }

    public void beginInsertion(int timeout) throws JposException {
        this.posPrinter.beginInsertion(timeout);
    }

    public void beginRemoval(int timeout) throws JposException {
        this.posPrinter.beginRemoval(timeout);
    }

    public void clearOutput() throws JposException {
        this.posPrinter.clearOutput();
    }

    public void cutPaper(int percentage) throws JposException {
        this.posPrinter.cutPaper(percentage);
    }

    public void endInsertion() throws JposException {
        this.posPrinter.endInsertion();
    }

    public void endRemoval() throws JposException {
        this.posPrinter.endRemoval();
    }

    public void printBarCode(int station, String data, int symbology, int height, int width, int alignment, int textPosition) throws JposException {
        this.posPrinter.printBarCode(station, data, symbology, height, width, alignment, textPosition);
    }

    public void printBitmap(int station, String fileName, int width, int alignment) throws JposException {
        this.posPrinter.printBitmap(station, fileName, width, alignment);
    }

    public void printImmediate(int station, String data) throws JposException {
        this.posPrinter.printImmediate(station, data);
    }

    public void printNormal(int station, String data) throws JposException {
        this.posPrinter.printNormal(station, data);
    }

    public void printTwoNormal(int stations, String data1, String data2) throws JposException {
        this.posPrinter.printTwoNormal(stations, data1, data2);
    }

    public void rotatePrint(int station, int rotation) throws JposException {
        this.posPrinter.rotatePrint(station, rotation);
    }

    public void setBitmap(int bitmapNumber, int station, String fileName, int width, int alignment) throws JposException {
        this.posPrinter.setBitmap(bitmapNumber, station, fileName, width, alignment);
    }

    public void setLogo(int location, String data) throws JposException {
        this.posPrinter.setLogo(location, data);
    }

    public void transactionPrint(int station, int control) throws JposException {
        this.posPrinter.transactionPrint(station, control);
    }

    public void validateData(int station, String data) throws JposException {
        this.posPrinter.validateData(station, data);
    }

    public void changePrintSide(int side) throws JposException {
        this.posPrinter.changePrintSide(side);
    }

    public void markFeed(int type) throws JposException {
        this.posPrinter.markFeed(type);
    }

    public void resetStatistics(String statisticsBuffer) throws JposException {
        this.posPrinter.resetStatistics(statisticsBuffer);
    }

    public void retrieveStatistics(String[] statisticsBuffer) throws JposException {
        this.posPrinter.retrieveStatistics(statisticsBuffer);
    }

    public void updateStatistics(String statisticsBuffer) throws JposException {
        this.posPrinter.updateStatistics(statisticsBuffer);
    }

    public void clearPrintArea() throws JposException {
        this.posPrinter.clearPrintArea();
    }

    public void compareFirmwareVersion(String firmwareFileName, int[] result) throws JposException {
        this.posPrinter.compareFirmwareVersion(firmwareFileName, result);
    }

    public void pageModePrint(int control) throws JposException {
        this.posPrinter.pageModePrint(control);
    }

    public void updateFirmware(String firmwareFileName) throws JposException {
        this.posPrinter.updateFirmware(firmwareFileName);
    }

    public void printMemoryBitmap(int station, byte[] data, int type, int width, int alignment) throws JposException {
        this.posPrinter.printMemoryBitmap(station, data, type, width, alignment);
    }

    public void drawRuledLine(int station, String positionList, int lineDirection, int lineWidth, int lineStyle, int lineColor) throws JposException {
        this.posPrinter.drawRuledLine(station, positionList, lineDirection, lineWidth, lineStyle, lineColor);
    }

    public void addDirectIOListener(DirectIOListener l) {
        this.posPrinter.addDirectIOListener(l);
    }

    public void removeDirectIOListener(DirectIOListener l) {
        this.posPrinter.removeDirectIOListener(l);
    }

    public void addErrorListener(ErrorListener l) {
        this.posPrinter.addErrorListener(l);
    }

    public void removeErrorListener(ErrorListener l) {
        this.posPrinter.removeErrorListener(l);
    }

    public void addOutputCompleteListener(OutputCompleteListener l) {
        this.posPrinter.addOutputCompleteListener(l);
    }

    public void removeOutputCompleteListener(OutputCompleteListener l) {
        this.posPrinter.removeOutputCompleteListener(l);
    }

    public void addStatusUpdateListener(StatusUpdateListener l) {
        this.posPrinter.addStatusUpdateListener(l);
    }

    public void removeStatusUpdateListener(StatusUpdateListener l) {
        this.posPrinter.removeStatusUpdateListener(l);
    }

    public String getCheckHealthText() throws JposException {
        return this.posPrinter.getCheckHealthText();
    }

    public boolean getClaimed() throws JposException {
        return this.posPrinter.getClaimed();
    }

    public String getDeviceControlDescription() {
        return this.posPrinter.getDeviceControlDescription();
    }

    public int getDeviceControlVersion() {
        return this.posPrinter.getDeviceControlVersion();
    }

    public boolean getDeviceEnabled() throws JposException {
        return this.posPrinter.getDeviceEnabled();
    }

    public void setDeviceEnabled(boolean deviceEnabled) throws JposException {
        this.posPrinter.setDeviceEnabled(deviceEnabled);
    }

    public String getDeviceServiceDescription() throws JposException {
        return this.posPrinter.getDeviceServiceDescription();
    }

    public int getDeviceServiceVersion() throws JposException {
        return this.posPrinter.getDeviceServiceVersion();
    }

    public boolean getFreezeEvents() throws JposException {
        return this.posPrinter.getFreezeEvents();
    }

    public void setFreezeEvents(boolean freezeEvents) throws JposException {
        this.posPrinter.setFreezeEvents(freezeEvents);
    }

    public String getPhysicalDeviceDescription() throws JposException {
        return this.posPrinter.getPhysicalDeviceDescription();
    }

    public String getPhysicalDeviceName() throws JposException {
        return this.posPrinter.getPhysicalDeviceName();
    }

    public int getState() {
        return this.posPrinter.getState();
    }

    public void claim(int timeout) throws JposException {
        this.posPrinter.claim(timeout);
    }

    public void checkHealth(int level) throws JposException {
        this.posPrinter.checkHealth(level);
    }

    public void directIO(int command, int[] data, Object object) throws JposException {
        this.posPrinter.directIO(command, data, object);
    }

    public void open(String logicalDeviceName) throws JposException {
        this.posPrinter.open(logicalDeviceName);
    }

    public void release() throws JposException {
        this.posPrinter.release();
    }
}
