package com.worldpay.hub.printer.commands;


public class Italic extends PrinterCommand
{
    public static final int ITALIC_ON   = 1;
    public static final int ITALIC_OFF  = 0;

    public Italic()
    {
        init(ITALIC_OFF);
    }

    public Italic(int type)
    {
        init(type);
    }

    protected void init(int type)
    {
        mData = new byte[] { 0x1B, 0x49, (byte)type };
    }
}
