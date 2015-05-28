package com.worldpay.hub.printer.commands;

/**
 * Prints the last downloaded bitmap
 */
public class PrintBitmap extends PrinterCommand
{
    public PrintBitmap()
    {
        mData = new byte[] { 0x1D, 0x2F, 0x00, 0x00 };
    }
}
