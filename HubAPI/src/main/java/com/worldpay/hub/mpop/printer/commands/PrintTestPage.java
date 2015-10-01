package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

public class PrintTestPage extends PrinterCommand
{
    public PrintTestPage()
    {
        mData = new byte[] { 0x1F, 0x74 };
    }
}
