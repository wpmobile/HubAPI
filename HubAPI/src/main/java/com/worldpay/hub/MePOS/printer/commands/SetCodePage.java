package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class SetCodePage extends PrinterCommand
{
    public static final int CODE_PAGE_437   = 0;
    public static final int CODE_PAGE_850   = 1;
    public static final int CODE_PAGE_852   = 2;
    public static final int CODE_PAGE_860   = 3;
    public static final int CODE_PAGE_863   = 4;
    public static final int CODE_PAGE_865   = 5;
    public static final int CODE_PAGE_858   = 6;
    public static final int CODE_PAGE_866   = 7;
    public static final int CODE_PAGE_1252  = 8;
    public static final int CODE_PAGE_862   = 9;
    public static final int CODE_PAGE_737   = 10;
    //There is no 11
    public static final int CODE_PAGE_857   = 12;

    public SetCodePage(int codepage)
    {
        mData = new byte[] { 0x1B, 0x52, (byte)codepage };
    }
}
