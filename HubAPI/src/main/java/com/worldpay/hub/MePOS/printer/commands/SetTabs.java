package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

/***
 * Sets the horizontal tab positions
 */
public class SetTabs extends PrinterCommand
{
    public SetTabs()
    {
        clearTabs();
    }

    public SetTabs(int... position)
    {
        tabs(position);
    }

    protected void tabs(int... position)
    {
        int len = position.length + 3;
        mData = new byte[len];

        mData[0] = 0x1B;
        mData[1] = 0x44;
        for (int i = 0; i < position.length; i++)
            mData[i + 2] = (byte)position[i];

        mData[len - 1] = 0x00;
    }

    public void clearTabs()
    {
        mData = new byte[] { 0x1B, 0x44, 0x00 };
    }
}
