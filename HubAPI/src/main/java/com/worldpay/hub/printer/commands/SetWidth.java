package com.worldpay.hub.printer.commands;

public class SetWidth extends PrinterCommand
{
    public SetWidth()
    {
        init(0);
    }

    public SetWidth(int width)
    {
        init(width);
    }

    protected void init(int width)
    {
        mData = new byte[] { 0x1D, 0x57,
                (byte) (width % 256),
                (byte) (width / 256) };
    }
}
