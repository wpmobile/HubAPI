package com.worldpay.hub.poszle.commands;

import com.worldpay.hub.PrinterCommand;

public class Beep extends PrinterCommand
{
    public Beep()
    {
        mData = new byte[] { 0x1D, 0x07 };
    }
}
