package com.worldpay.hub.printer.commands;

/**
 * Switch between volatile RAM or flash storage
 */
public class SelectMemory extends PrinterCommand
{
    public static final byte MEMORY_RAM     = 0x30;
    public static final byte MEMORY_FLASH   = 0x31;

    public SelectMemory()
    {
        mData = new byte[] { 0x1D, 0x22, MEMORY_FLASH};
    }

    public SelectMemory(byte memoryLocation)
    {
        mData = new byte[] { 0x1D, 0x22, memoryLocation};
    }
}
