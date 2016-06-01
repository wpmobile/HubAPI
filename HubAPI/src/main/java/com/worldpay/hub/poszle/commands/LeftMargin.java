package com.worldpay.hub.poszle.commands;
import com.worldpay.hub.PrinterCommand;

public class LeftMargin extends PrinterCommand
{
    public LeftMargin()
    {
        setMargin(0);
    }

    public LeftMargin(int margin)
    {
        setMargin(margin);
    }

    public void setMargin(int margin)
    {
        mData = new byte[] { 0x1D, 0x6C, (byte) margin };
    }
}
