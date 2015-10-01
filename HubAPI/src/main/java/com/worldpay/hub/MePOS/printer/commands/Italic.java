package com.worldpay.hub.MePOS.printer.commands;


import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterFactory;

public class Italic extends PrinterCommand
{

    public Italic()
    {
        init(PrinterFactory.ITALIC_OFF);
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
