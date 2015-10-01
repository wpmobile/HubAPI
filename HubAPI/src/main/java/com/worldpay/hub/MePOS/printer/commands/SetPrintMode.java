package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.PrinterCommand;

public class SetPrintMode extends PrinterCommand
{
    boolean mIsBold = false;
    boolean mIsCompressed = false;
    boolean mIsDoubleHigh = false;
    boolean mIsDoubleWide = false;
    boolean mIsUnderlined = false;
    public SetPrintMode()
    {
        update();
    }

    public void setBold(boolean isBold)
    {
        mIsBold = isBold;

        update();
    }

    public void setCompressed(boolean isCompressed)
    {
        mIsCompressed = isCompressed;
        update();
    }

    public void setDoubleHeight(boolean isTall)
    {
        mIsDoubleHigh = isTall;
        update();
    }

    public void setDoubleWidth(boolean isWide)
    {
        mIsDoubleWide = isWide;
        update();
    }

    public void setUnderlined(boolean isUnderlined)
    {
        mIsUnderlined = isUnderlined;
        update();
    }

    protected void update()
    {
        byte setting = 0x00;
        if(mIsCompressed)
            setting |= 0x01;
        if(mIsBold)
            setting |= 0x04;
        if(mIsDoubleHigh)
            setting |= 0x8;
        if(mIsDoubleWide)
            setting |= 0x10;
        if(mIsUnderlined)
            setting |= 0x40;

        mData = new byte[] { 0x1B, 0x21, setting };
    }
}
