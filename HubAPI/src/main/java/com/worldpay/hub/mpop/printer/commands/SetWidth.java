package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

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
        mData = new byte[] { 0x1D, 0x1F, 0x41, (byte) width };
    }
}
