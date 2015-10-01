package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

public class InitialisePrinter extends PrinterCommand
{
    public InitialisePrinter()
    {
        mData = new byte[] { 0x1B, 0x40 };
    }
}
