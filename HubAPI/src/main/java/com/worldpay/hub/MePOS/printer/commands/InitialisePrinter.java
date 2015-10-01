package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class InitialisePrinter extends PrinterCommand
{
    public InitialisePrinter()
    {
        mData = new byte[] { 0x1B, 0x40 };
    }
}
