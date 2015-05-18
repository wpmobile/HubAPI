package com.worldpay.hub.printer.commands;

public class ScriptMode extends PrinterCommand
{
    public static final int SUPERSCRIPT = 2;
    public static final int SUBSCRIPT   = 1;
    public static final int NORMAL      = 0;

    public ScriptMode(int type)
    {
        mData = new byte[] { 0x1F, 0x05, (byte) type };
    }
}
