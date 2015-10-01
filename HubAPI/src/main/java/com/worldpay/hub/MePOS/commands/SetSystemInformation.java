package com.worldpay.hub.MePOS.commands;

public class SetSystemInformation extends Command
{
    protected SystemInformation mSystemInformation;

    public SetSystemInformation()
    {
        mCommand = 'Y';
        mSystemInformation = new SystemInformation();
    }

    public SetSystemInformation(SystemInformation si)
    {
        mCommand = 'Y';
        mSystemInformation = si;
    }

    @Override
    public byte[] getCommandData()
    {
        return mSystemInformation.getCommandData();
    }

    @Override
    public void setCommandData(byte[] data)
    {
        mSystemInformation.deserialise(data);
    }

    public void setSystemInformation(SystemInformation si)
    {
        mSystemInformation = si;
    }

    public SystemInformation getSystemInformation()
    {
        return mSystemInformation;
    }
}
