package com.worldpay.hub.poszle.commands;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;

/**
 * Switch between volatile RAM or flash storage
 */
public class SelectMemory extends PrinterCommand
{
    public static final byte MEMORY_RAM     = 0x30;
    public static final byte MEMORY_FLASH   = 0x31;

    public SelectMemory() throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }

    public SelectMemory(byte memoryLocation) throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }
}
