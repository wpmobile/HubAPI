package com.worldpay.hub.printer.commands;

public class ReversePrintMode extends PrinterCommand
{
    public static final int REVERSE_OFF = 0;
    public static final int REVERSE_ON  = 1;

    public ReversePrintMode(int printMode)
    {
        mData = new byte[] { 0x1D, 0x42, (byte) printMode };
    }
}
