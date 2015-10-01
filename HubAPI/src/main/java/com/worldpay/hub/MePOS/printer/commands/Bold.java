package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class Bold extends PrinterCommand
{
    public static final int BOLD_ON   = 1;
    public static final int BOLD_OFF  = 0;

    public Bold()
    {
        init(BOLD_OFF);
    }

    public Bold(int type)
    {
        init(type);
    }

    protected void init(int type)
    {
        mData = new byte[] { 0x1B, 0x45, (byte)type };
    }
}
