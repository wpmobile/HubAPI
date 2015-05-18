package com.worldpay.hub.printer.commands;

public class SingleWidthCharacters extends PrinterCommand
{
    public SingleWidthCharacters()
    {
        mData = new byte[] { 0x13 };
    }
}
