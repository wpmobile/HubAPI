package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

/***
 * Flushes the last line to the printer.  This is the same as a line feed in the
 * text (\n)
 */
public class Flush extends PrinterCommand
{
    public Flush()
    {
        mData = new byte[]{ 0x0D, 0x0A };

        //Add a 1 ms delay so that the queue knows to send this
        mDelay = 1;
    }
}
