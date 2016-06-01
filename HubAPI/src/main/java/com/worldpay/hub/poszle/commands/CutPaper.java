package com.worldpay.hub.poszle.commands;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterFactory;

public class CutPaper extends PrinterCommand
{

    public CutPaper()
    {
        init(PrinterFactory.CUT_FULL);
    }

    public CutPaper(int type)
    {
        init(type);
    }

    protected void init(int type)
    {
        mData = new byte[] { 0x1B, 0x64, 0x03 };
    }
}
