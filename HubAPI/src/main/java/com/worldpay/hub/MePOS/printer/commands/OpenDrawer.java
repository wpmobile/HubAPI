package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class OpenDrawer extends PrinterCommand
{
    public OpenDrawer()
    {
        mData = new byte[] { 0x07 };
    }
}
