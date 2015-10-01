package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class Justify extends PrinterCommand
{
    public static final int JUSTIFY_LEFT    = 48;
    public static final int JUSTIFY_CENTRE  = 49;
    public static final int JUSTIFY_RIGHT   = 50;

    public Justify()
    {
        init(JUSTIFY_LEFT);
    }

    public Justify(int pos)
    {
        init(pos);
    }

    protected void init(int pos)
    {
        mData = new byte[] { 0x1B, 0x61, (byte)pos };
    }
}
