package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class LeftMargin extends PrinterCommand
{
    public LeftMargin()
    {
        setMargin(0);
    }

    public LeftMargin(int margin)
    {
        setMargin(margin);
    }

    public void setMargin(int margin)
    {
        mData = new byte[] { 0x1D, 0x4C,
                (byte) (margin % 256),
                (byte) (margin / 256)};
    }
}
