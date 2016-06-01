package com.worldpay.hub.poszle.commands;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;

/**
 * Erase the flash memory.  This takes time to execute, but there will not be any
 * feedback to know when this operation has completed.  Trial and error!
 */
public class EraseMemory extends PrinterCommand
{
    public EraseMemory() throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }
}
