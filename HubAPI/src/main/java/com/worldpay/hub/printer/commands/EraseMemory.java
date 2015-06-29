package com.worldpay.hub.printer.commands;

/**
 * Erase the flash memory.  This takes time to execute, but there will not be any
 * feedback to know when this operation has completed.  Trial and error!
 */
public class EraseMemory extends PrinterCommand
{
    public EraseMemory()
    {
        mData = new byte[] { 0x1D, 0x40, 0x31 };
    }
}
