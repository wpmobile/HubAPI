package com.worldpay.hub.printer.commands;

public class PrintTestPage extends PrinterCommand
{
    public PrintTestPage()
    {
        mData = new byte[] { 0x1F, 0x74 };
    }
}
