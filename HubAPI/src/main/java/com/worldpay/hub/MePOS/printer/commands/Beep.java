package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class Beep extends PrinterCommand
{
    public Beep()
    {
        mData = new byte[] { 0x1B, 0x07 };
    }
}
