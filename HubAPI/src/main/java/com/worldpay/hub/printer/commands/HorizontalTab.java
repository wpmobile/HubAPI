package com.worldpay.hub.printer.commands;

/***
 * Moves the print position to the next tab position set by the set horizontal tab positions
 * This is the same as adding \t into text to print
 */
public class HorizontalTab extends PrinterCommand
{
    public HorizontalTab()
    {
        mData = new byte[] { 0x09 };
    }
}
