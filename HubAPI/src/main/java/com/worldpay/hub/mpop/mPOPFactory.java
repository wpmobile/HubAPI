package com.worldpay.hub.mpop;

import android.graphics.Bitmap;

import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;
import com.worldpay.hub.PrinterFactory;
import com.worldpay.hub.PrinterQueue;
import com.worldpay.hub.mpop.printer.commands.Beep;
import com.worldpay.hub.mpop.printer.commands.Bold;
import com.worldpay.hub.mpop.printer.commands.ClearPrinter;
import com.worldpay.hub.mpop.printer.commands.CutPaper;
import com.worldpay.hub.mpop.printer.commands.DoubleWidthCharacters;
import com.worldpay.hub.mpop.printer.commands.DownloadBitmap;
import com.worldpay.hub.mpop.printer.commands.EraseMemory;
import com.worldpay.hub.mpop.printer.commands.FeedPaper;
import com.worldpay.hub.mpop.printer.commands.Flush;
import com.worldpay.hub.mpop.printer.commands.GetStatus;
import com.worldpay.hub.mpop.printer.commands.HorizontalTab;
import com.worldpay.hub.mpop.printer.commands.InitialisePrinter;
import com.worldpay.hub.mpop.printer.commands.Italic;
import com.worldpay.hub.mpop.printer.commands.Justify;
import com.worldpay.hub.mpop.printer.commands.LeftMargin;
import com.worldpay.hub.mpop.printer.commands.OpenDrawer;
import com.worldpay.hub.mpop.printer.commands.PrintBitmap;
import com.worldpay.hub.mpop.printer.commands.PrintTestPage;
import com.worldpay.hub.mpop.printer.commands.PrintText;
import com.worldpay.hub.mpop.printer.commands.ReversePrintMode;
import com.worldpay.hub.mpop.printer.commands.SelectMemory;
import com.worldpay.hub.mpop.printer.commands.SetCharacterSet;
import com.worldpay.hub.mpop.printer.commands.SetCodePage;
import com.worldpay.hub.mpop.printer.commands.SetTabs;
import com.worldpay.hub.mpop.printer.commands.SetWidth;
import com.worldpay.hub.mpop.printer.commands.SingleWidthCharacters;
import com.worldpay.hub.mpop.printer.commands.Underline;

/**
 * mPoP implemetnation of the printer factory
 */
public class mPOPFactory implements PrinterFactory
{
    @Override
    public PrinterCommand Beep() throws PrinterCommandNotImplementedException
    {
        return new Beep();
    }

    @Override
    public PrinterCommand Bold() throws PrinterCommandNotImplementedException
    {
        return new Bold();
    }

    @Override
    public PrinterCommand Bold(int mode) throws PrinterCommandNotImplementedException
    {
        return new Bold(mode);
    }

    @Override
    public PrinterCommand ClearPrinter() throws PrinterCommandNotImplementedException
    {
        return new ClearPrinter();
    }

    @Override
    public PrinterCommand CutPaper() throws PrinterCommandNotImplementedException
    {
        return new CutPaper();
    }

    @Override
    public PrinterCommand CutPaper(int type) throws PrinterCommandNotImplementedException
    {
        return new CutPaper(type);
    }

    @Override
    public PrinterCommand DoubleWidthCharacters() throws PrinterCommandNotImplementedException
    {
        return new DoubleWidthCharacters();
    }

    @Override
    public PrinterCommand DownloadBitmap(byte[] data) throws PrinterCommandNotImplementedException
    {
        return new DownloadBitmap(data);
    }

    @Override
    public PrinterCommand EraseMemory() throws PrinterCommandNotImplementedException
    {
        return new EraseMemory();
    }

    @Override
    public PrinterCommand FeedPaper() throws PrinterCommandNotImplementedException
    {
        return new FeedPaper();
    }

    @Override
    public PrinterCommand FeedPaper(int lines) throws PrinterCommandNotImplementedException
    {
        return new FeedPaper(lines);
    }

    @Override
    public PrinterCommand Flush() throws PrinterCommandNotImplementedException
    {
        return new Flush();
    }

    @Override
    public PrinterCommand GetStatus(byte status) throws PrinterCommandNotImplementedException
    {
        return new GetStatus(status);
    }

    @Override
    public PrinterCommand HorizontalTab() throws PrinterCommandNotImplementedException
    {
        return new HorizontalTab();
    }

    @Override
    public PrinterCommand InitialisePrinter() throws PrinterCommandNotImplementedException
    {
        return new InitialisePrinter();
    }

    @Override
    public PrinterCommand Italic() throws PrinterCommandNotImplementedException
    {
        return new Italic();
    }

    @Override
    public PrinterCommand Italic(int onOff) throws PrinterCommandNotImplementedException
    {
        return new Italic(onOff);
    }

    @Override
    public PrinterCommand Justify() throws PrinterCommandNotImplementedException
    {
        return new Justify();
    }

    @Override
    public PrinterCommand Justify(int position) throws PrinterCommandNotImplementedException
    {
        return new Justify(position);
    }

    @Override
    public PrinterCommand LeftMargin() throws PrinterCommandNotImplementedException
    {
        return new LeftMargin();
    }

    @Override
    public PrinterCommand LeftMargin(int margin) throws PrinterCommandNotImplementedException
    {
        return new LeftMargin(margin);
    }

    @Override
    public PrinterCommand OpenDrawer() throws PrinterCommandNotImplementedException
    {
        return new OpenDrawer();
    }

    @Override
    public PrinterCommand PrintBitmap() throws PrinterCommandNotImplementedException
    {
        return new PrintBitmap();
    }

    @Override
    public PrinterCommand PrintBitmap(int index) throws PrinterCommandNotImplementedException
    {
        return new PrintBitmap(index);
    }

    @Override
    public PrinterCommand PrintBitmap(Bitmap bitmap, int width, int rotation) throws PrinterCommandNotImplementedException
    {
        return new PrintBitmap(bitmap, width, rotation);
    }

    @Override
    public PrinterCommand PrintTestPage() throws PrinterCommandNotImplementedException
    {
        return new PrintTestPage();
    }

    @Override
    public PrinterCommand PrintText(String text) throws PrinterCommandNotImplementedException
    {
        return new PrintText(text);
    }

    @Override
    public PrinterCommand ReversePrintMode() throws PrinterCommandNotImplementedException
    {
        return new ReversePrintMode();
    }

    @Override
    public PrinterCommand ReversePrintMode(int onOff) throws PrinterCommandNotImplementedException
    {
        return new ReversePrintMode(onOff);
    }

    @Override
    public PrinterCommand SelectMemory() throws PrinterCommandNotImplementedException
    {
        return new SelectMemory();
    }

    @Override
    public PrinterCommand SelectMemory(byte memoryLocation) throws PrinterCommandNotImplementedException
    {
        return new SelectMemory(memoryLocation);
    }

    @Override
    public PrinterCommand SetCodePage(int codePage) throws PrinterCommandNotImplementedException
    {
        return new SetCodePage(codePage);
    }

    @Override
    public PrinterCommand SetTabs() throws PrinterCommandNotImplementedException
    {
        return new SetTabs();
    }

    @Override
    public PrinterCommand SetTabs(int... positions) throws PrinterCommandNotImplementedException
    {
        return new SetTabs(positions);
    }

    @Override
    public PrinterCommand SetWidth(int width) throws PrinterCommandNotImplementedException
    {
        return new SetWidth(width);
    }

    @Override
    public PrinterCommand SingleWidthCharacters() throws PrinterCommandNotImplementedException
    {
        return new SingleWidthCharacters();
    }

    @Override
    public PrinterCommand Underline(int mode) throws PrinterCommandNotImplementedException
    {
        return new Underline(mode);
    }

    @Override
    public PrinterCommand SetCharacterSet(int mode) throws PrinterCommandNotImplementedException
    {
        return new SetCharacterSet(mode);
    }

    @Override
    public PrinterCommand RasterPrint(byte[] picture) throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }

    @Override
    public void PrintRasterImage(PrinterQueue queue, byte[] picture)
    {

    }
}
