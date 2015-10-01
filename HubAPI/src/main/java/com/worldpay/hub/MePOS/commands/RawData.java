package com.worldpay.hub.MePOS.commands;

public class RawData extends Command
{
    public RawData()
    {
        mCommand = 'N';
        mCommandData = new byte[0];
    }

    public RawData(byte[] data)
    {
        mCommand = 'N';
        mCommandData = data;
    }
}
