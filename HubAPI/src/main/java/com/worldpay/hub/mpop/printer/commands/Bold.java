package com.worldpay.hub.mpop.printer.commands;

import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterFactory;

public class Bold extends PrinterCommand
{
    public Bold()
    {
        init(PrinterFactory.BOLD_OFF);
    }

    public Bold(int type)
    {
        init(type);
    }

    protected void init(int type)
    {
        mData = new byte[] { 0x1B, (byte)type };
    }
}
