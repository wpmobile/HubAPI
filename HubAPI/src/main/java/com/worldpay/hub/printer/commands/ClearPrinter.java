package com.worldpay.hub.printer.commands;

public class ClearPrinter extends PrinterCommand
{
    public ClearPrinter()
    {
        mData = new byte[] { 0x10 };
    }
}
