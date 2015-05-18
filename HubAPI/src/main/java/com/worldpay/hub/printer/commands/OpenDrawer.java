package com.worldpay.hub.printer.commands;

public class OpenDrawer extends PrinterCommand
{
    public OpenDrawer()
    {
        mData = new byte[] { 0x1B, 0x07, /* ring the bell */
                             0x1B, 0x70, 0x00, 0x48, 0x7F, 0x7F  /* open the drawer */};
    }
}
