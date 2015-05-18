package com.worldpay.hub.printer.commands;

public class Underline extends PrinterCommand
{
    public static final int UNDERLINE_NONE   = 0x30;
    public static final int UNDERLINE_SINGLE = 0x31;
    public static final int UNDERLINE_DOUBLE = 0x32;

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
