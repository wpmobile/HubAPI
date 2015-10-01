package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class ClearPrinter extends PrinterCommand
{
    public ClearPrinter()
    {
        mData = new byte[] { 0x10 };
    }
}
