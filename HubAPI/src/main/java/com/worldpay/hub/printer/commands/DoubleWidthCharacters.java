package com.worldpay.hub.printer.commands;

public class DoubleWidthCharacters extends PrinterCommand
{
    public DoubleWidthCharacters()
    {
        mData = new byte[] { 0x12 };
    }
}
