package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

public class Underline extends PrinterCommand
{
    public static final int UNDERLINE_NONE   = 0x00;
    public static final int UNDERLINE_SINGLE = 0x01;

    public Underline()
    {
        init(UNDERLINE_NONE);
    }

    public Underline(int type)
    {
        init(type);
    }

    protected void init(int type)
    {
        mData = new byte[] { 0x1B, 0x2D, (byte)type };
    }
}
