package com.worldpay.hub.printer.commands;

public class CutPaper extends PrinterCommand
{
    public static final int CUT_FULL = 0;
    public static final int CUT_PARTIAL = 1;

    public CutPaper()
    {
        init(CUT_FULL);
    }

    public CutPaper(int type)
    {
        init(type);
    }

    protected void init(int type)
    {
        if(type == CUT_FULL)
            mData = new byte[] { 0x19 };
        else
            mData = new byte[] { 0x1A };
    }
}
