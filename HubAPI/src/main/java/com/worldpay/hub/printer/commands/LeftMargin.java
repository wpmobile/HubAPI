package com.worldpay.hub.printer.commands;

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
