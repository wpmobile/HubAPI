package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class SingleWidthCharacters extends PrinterCommand
{
    public SingleWidthCharacters()
    {
        mData = new byte[] { 0x13 };
    }
}
