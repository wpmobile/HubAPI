package com.worldpay.hub;

import android.graphics.Bitmap;

/**
 * A common interface for all printer factories.
 */
public interface PrinterFactory
{

    public static final int BOLD_ON   = 0x45;
    public static final int BOLD_OFF  = 0x46;

    public static final int CUT_FULL = 0;
    public static final int CUT_PARTIAL = 1;

    public static final int ITALIC_ON   = 1;
    public static final int ITALIC_OFF  = 0;

    public static final int REVERSE_OFF = 0;
    public static final int REVERSE_ON  = 1;

    public static final int UNDERLINE_NONE   = 0x00;
    public static final int UNDERLINE_SINGLE = 0x01;

    public static final int ROTATION_90 = 1;
    public static final int ROTATION_180 = 2;
    public static final int ROTATION_270 = 3;
    public static final int ROTATION_0 = 0;

    public PrinterCommand Beep() throws PrinterCommandNotImplementedException;
    public PrinterCommand Bold() throws PrinterCommandNotImplementedException;
    public PrinterCommand Bold(int mode) throws PrinterCommandNotImplementedException;
    public PrinterCommand ClearPrinter() throws PrinterCommandNotImplementedException;
    public PrinterCommand CutPaper() throws PrinterCommandNotImplementedException;
    public PrinterCommand CutPaper(int type) throws PrinterCommandNotImplementedException;
    public PrinterCommand DoubleWidthCharacters() throws PrinterCommandNotImplementedException;
    public PrinterCommand DownloadBitmap(byte[] bitmap) throws PrinterCommandNotImplementedException;
    public PrinterCommand EraseMemory() throws PrinterCommandNotImplementedException;
    public PrinterCommand FeedPaper() throws PrinterCommandNotImplementedException;
    public PrinterCommand FeedPaper(int lines) throws PrinterCommandNotImplementedException;
    public PrinterCommand Flush() throws PrinterCommandNotImplementedException;
    public PrinterCommand GetStatus(byte status) throws PrinterCommandNotImplementedException;
    public PrinterCommand HorizontalTab() throws PrinterCommandNotImplementedException;
    public PrinterCommand InitialisePrinter() throws PrinterCommandNotImplementedException;
    public PrinterCommand Italic() throws PrinterCommandNotImplementedException;
    public PrinterCommand Italic(int onOff) throws PrinterCommandNotImplementedException;
    public PrinterCommand Justify() throws PrinterCommandNotImplementedException;
    public PrinterCommand Justify(int position) throws PrinterCommandNotImplementedException;
    public PrinterCommand LeftMargin() throws PrinterCommandNotImplementedException;
    public PrinterCommand LeftMargin(int margin) throws PrinterCommandNotImplementedException;
    public PrinterCommand OpenDrawer() throws PrinterCommandNotImplementedException;
    public PrinterCommand PrintBitmap() throws PrinterCommandNotImplementedException;
    public PrinterCommand PrintBitmap(int index) throws PrinterCommandNotImplementedException;
    public PrinterCommand PrintBitmap(Bitmap bitmap, int width, int rotation) throws PrinterCommandNotImplementedException;
    public PrinterCommand PrintTestPage() throws PrinterCommandNotImplementedException;
    public PrinterCommand PrintText(String text) throws PrinterCommandNotImplementedException;
    public PrinterCommand ReversePrintMode() throws PrinterCommandNotImplementedException;
    public PrinterCommand ReversePrintMode(int onOff) throws PrinterCommandNotImplementedException;
    public PrinterCommand SelectMemory() throws PrinterCommandNotImplementedException;
    public PrinterCommand SelectMemory(byte memoryLocation) throws PrinterCommandNotImplementedException;
    public PrinterCommand SetCodePage(int codePage) throws PrinterCommandNotImplementedException;
    public PrinterCommand SetTabs() throws PrinterCommandNotImplementedException;
    public PrinterCommand SetTabs(int... position) throws PrinterCommandNotImplementedException;
    public PrinterCommand SetWidth(int width) throws PrinterCommandNotImplementedException;
    public PrinterCommand SingleWidthCharacters() throws PrinterCommandNotImplementedException;
    public PrinterCommand Underline(int mode) throws PrinterCommandNotImplementedException;
    public PrinterCommand SetCharacterSet(int set) throws PrinterCommandNotImplementedException;
    public PrinterCommand RasterPrint(byte[] picture) throws PrinterCommandNotImplementedException;
    public void PrintRasterImage(PrinterQueue queue, byte[] picture);
}
