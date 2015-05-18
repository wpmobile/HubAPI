package com.worldpay.hub.printer.commands;

public class InitialisePrinter extends PrinterCommand
{
    public InitialisePrinter()
    {
        mData = new byte[] { 0x1B, 0x40 };
    }
}
