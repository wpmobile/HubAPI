package com.worldpay.hub.printer.commands;

public class Beep extends PrinterCommand
{
    public Beep()
    {
        mData = new byte[] { 0x1B, 0x07 };
    }
}
