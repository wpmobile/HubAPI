package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class DoubleWidthCharacters extends PrinterCommand
{
    public DoubleWidthCharacters()
    {
        mData = new byte[] { 0x12 };
    }
}
