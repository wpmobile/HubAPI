package com.worldpay.hub.printer.commands;

/**
 * Prints the last downloaded bitmap
 */
public class PrintBitmap extends PrinterCommand
{
    public PrintBitmap()
    {
        init(0);
    }

    public PrintBitmap(int index)
    {
        init(index);
    }

    private void init(int index)
    {
        mData = new byte[] { 0x1D, 0x2F, (byte) index, 0x00 };
    }
}
