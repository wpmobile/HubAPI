package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterFactory;

public class ReversePrintMode extends PrinterCommand
{
    public ReversePrintMode()
    {
        init(PrinterFactory.REVERSE_ON);
    }

    public ReversePrintMode(int printMode)
    {
        init(printMode);
    }

    protected void init(int mode)
    {
        byte printMode = 0x34;
        if(mode == PrinterFactory.REVERSE_OFF)
            printMode = 0x35;

        mData = new byte[] { 0x1B, printMode };
    }
}
