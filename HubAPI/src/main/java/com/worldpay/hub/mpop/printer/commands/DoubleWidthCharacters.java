package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

public class DoubleWidthCharacters extends PrinterCommand
{
    public DoubleWidthCharacters()
    {
        mData = new byte[] { 0x1B, 0x57, 0x01 };
    }
}
