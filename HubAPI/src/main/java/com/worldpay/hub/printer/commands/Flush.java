package com.worldpay.hub.printer.commands;

/***
 * Flushes the last line to the printer.  This is the same as a line feed in the
 * text (\n)
 */
public class Flush extends PrinterCommand
{
    public Flush()
    {
        mData = new byte[]{ 0x0D, 0x0A };
    }
}
