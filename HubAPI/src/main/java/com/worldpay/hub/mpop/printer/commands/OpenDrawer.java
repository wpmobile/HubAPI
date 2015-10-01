package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

public class OpenDrawer extends PrinterCommand
{
    public OpenDrawer()
    {
        mData = new byte[] { 0x1B, 0x1D, 0x07, 0x01, 0x7F, 0x7F };
    }
}
