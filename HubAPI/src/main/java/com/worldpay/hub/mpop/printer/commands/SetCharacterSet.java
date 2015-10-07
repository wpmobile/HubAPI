package com.worldpay.hub.mpop.printer.commands;

import com.worldpay.hub.PrinterCommand;

/**
 * Created by Micro2 on 02/10/2015.
 */
public class SetCharacterSet extends PrinterCommand
{
    public static final int USA             = 0;
    public static final int FRANCE          = 1;
    public static final int GERMANY         = 2;
    public static final int UK              = 3;
    public static final int DENMARK         = 4;
    public static final int SWEDEN          = 5;
    public static final int ITALY           = 6;
    public static final int SPAIN           = 7;
    public static final int JAPAN           = 8;
    public static final int NORWAY          = 9;
    public static final int DENMARKII       = 10;
    public static final int SPAINII         = 11;
    public static final int LATIN_AMERICA   = 12;
    public static final int KOREA           = 13;
    public static final int IRELAND         = 14;
    public static final int LEGAL           = 64;

    public SetCharacterSet(int characterSet)
    {
        mData = new byte[] { 0x1B, 0x52, (byte)characterSet };
    }
}
