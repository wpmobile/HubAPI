package com.worldpay.hub.poszle.commands;

import com.worldpay.hub.PrinterCommand;

public class SingleWidthCharacters extends PrinterCommand
{
    public SingleWidthCharacters()
    {
        mData = new byte[] { 0x1B, 0x57, 0x00 };
    }
}
