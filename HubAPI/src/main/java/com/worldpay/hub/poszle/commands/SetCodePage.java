package com.worldpay.hub.poszle.commands;
import com.worldpay.hub.PrinterCommand;

public class SetCodePage extends PrinterCommand
{
    public static final int KATAKANA        = 2; //Katakana
    public static final int CODE_PAGE_437   = 3; //Western Europe
    public static final int CODE_PAGE_852   = 5; //Latin -2
    public static final int CODE_PAGE_860   = 6; //Portugeuse
    public static final int CODE_PAGE_861   = 7; //Icelandic
    public static final int CODE_PAGE_863   = 8; //Canadian French
    public static final int CODE_PAGE_865   = 9; //Nordic
    public static final int CODE_PAGE_866   = 10; //Cyrillic Russian
    public static final int CODE_PAGE_855   = 10; //Cyrillic Bulgarian
    public static final int CODE_PAGE_857   = 12; //Turkish
    public static final int CODE_PAGE_858   = 4; //Multilingual
    public static final int CODE_PAGE_1252  = 32; //Windows Latin -2
    public static final int CODE_PAGE_862   = 13; //Hebrew
    public static final int CODE_PAGE_864   = 14; //Arabic
    public static final int CODE_PAGE_737   = 15; //Greek

    public SetCodePage(int codepage)
    {
        mData = new byte[] { 0x1B, 0x1D, 0x74, (byte)codepage };
    }
}
